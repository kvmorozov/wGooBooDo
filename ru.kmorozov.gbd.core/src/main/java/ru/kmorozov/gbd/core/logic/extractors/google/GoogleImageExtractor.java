package ru.kmorozov.gbd.core.logic.extractors.google;

import com.google.common.base.Strings;
import ru.kmorozov.db.core.logic.model.book.google.GoogleBookData;
import ru.kmorozov.db.core.logic.model.book.google.GooglePageInfo;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractImageExtractor;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.gbd.utils.Images;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static ru.kmorozov.gbd.core.config.constants.GoogleConstants.DEFAULT_PAGE_WIDTH;

/**
 * Created by km on 21.11.2015.
 */
public class GoogleImageExtractor extends AbstractImageExtractor {

    private final AtomicInteger proxyReceived = new AtomicInteger(0);
    private final AtomicBoolean processingStarted = new AtomicBoolean(false);

    public GoogleImageExtractor(BookContext bookContext) {
        super(bookContext, GoogleImageExtractor.class);
    }

    @Override
    protected void restoreState() {
        int imgWidth = 0 == GBDOptions.getImageWidth() ? DEFAULT_PAGE_WIDTH : GBDOptions.getImageWidth();

        try {
            this.bookContext.getPagesStream().filter(IPage::isFileExists).forEach(page -> {
                try {
                    if (!this.bookContext.getStorage().isPageExists(page)) {
                        this.logger.severe(String.format("Page %s not found in directory!", page.getPid()));
                        ((AbstractPage) page).setDataProcessed(false);
                        ((AbstractPage) page).setFileExists(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            final Stream<Path> files = this.bookContext.getStorage().getFiles();
            if (files == null)
                return;

            files.forEach(filePath -> {
                this.setProgress(this.bookContext.getProgress().incrementAndProgress());

                if (Images.isImageFile(filePath)) {
                    String fileName = filePath.getFileName().toString();
                    String[] nameParts = fileName.split("\\.")[0].split("_");
                    GooglePageInfo _page = (GooglePageInfo) this.bookContext.getBookInfo().getPages().getPageByPid(nameParts[1]);
                    int order = Integer.valueOf(nameParts[0]);
                    if (null == _page) {
                        this.logger.severe(String.format("Page %s not found!", fileName));
                        try {
                            Files.delete(filePath);
                            this.logger.severe(String.format("Page %s deleted!", fileName));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            if (GBDOptions.reloadImages()) {
                                BufferedImage bimg = ImageIO.read(new File(filePath.toString()));
                                _page.setWidth(bimg.getWidth());
                                _page.setDataProcessed(bimg.getWidth() >= imgWidth);

                                // 1.4 - эмпирически, высота переменная
                                if ((double) bimg.getWidth() * 1.4 > (double) bimg.getHeight()) {
                                    Files.delete(filePath);
                                    _page.setDataProcessed(false);
                                    this.logger.severe(String.format("Page %s deleted!", _page.getPid()));
                                }
                            } else _page.setDataProcessed(true);

                            if (Images.isInvalidImage(filePath, imgWidth)) {
                                _page.setDataProcessed(false);
                                Files.delete(filePath);
                                this.logger.severe(String.format("Page %s deleted!", _page.getPid()));
                            } else if (_page.getOrder() != order && !_page.isGapPage()) {
                                File oldFile = filePath.toFile();
                                File newFile = new File(filePath.toString().replace(order + "_", _page.getOrder() + "_"));
                                if (!newFile.exists())
                                    oldFile.renameTo(newFile);
                                _page.setDataProcessed(true);
                                this.logger.severe(String.format("Page %s renamed!", _page.getPid()));
                            }
                        } catch (IOException e) {
                            // Значит файл с ошибкой
                            (new File(filePath.toString())).delete();
                            _page.setDataProcessed(false);
                            this.logger.severe(String.format("Page %s deleted!", _page.getPid()));
                        }

                        _page.setFileExists(true);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != this.bookContext.getProgress()) this.bookContext.getProgress().finish();
        }

        this.bookContext.setPagesBefore(this.bookContext.getPagesStream().filter(IPage::isFileExists).count());
    }

    private void setProgress(int i) {
    }

    @Override
    protected boolean preCheck() {
        if (!Strings.isNullOrEmpty(((GoogleBookData) this.bookContext.getBookInfo().getBookData()).getFlags().getDownloadPdfUrl())) {
            this.logger.severe("There is direct url to download book. DIY!");
            return false;
        } else return true;
    }

    @Override
    protected void prepareStorage() {
        super.prepareStorage();
        this.bookContext.getBookInfo().getPages().build();
    }

    @Override
    public void newProxyEvent(HttpHostExt proxy) {
        if (null != proxy)
            (new Thread(new EventProcessor(proxy))).start();
    }

    private class EventProcessor implements Runnable {

        private final HttpHostExt proxy;

        EventProcessor(HttpHostExt proxy) {
            this.proxy = proxy;
        }

        @Override
        public void run() {
            while (!GoogleImageExtractor.this.initComplete.get()) {
                GoogleImageExtractor.this.waitingProxy.add(this.proxy);
                return;
            }

            if (this.proxy.isAvailable()) GoogleImageExtractor.this.bookContext.sigExecutor.execute(new GooglePageSigProcessor(GoogleImageExtractor.this.bookContext, this.proxy));

            int proxyNeeded = ExecutionContext.getProxyCount() - GoogleImageExtractor.this.proxyReceived.incrementAndGet();

            if (0 >= proxyNeeded) {
                if (!GoogleImageExtractor.this.processingStarted.compareAndSet(false, true)) return;

                GoogleImageExtractor.this.bookContext.sigExecutor.terminate(10L, TimeUnit.MINUTES);

                GoogleImageExtractor.this.bookContext.getPagesStream().filter(page -> !page.isDataProcessed() && null != ((GooglePageInfo) page).getSig()).forEach(page -> GoogleImageExtractor.this.bookContext.imgExecutor.execute(new GooglePageImgProcessor(GoogleImageExtractor.this.bookContext, (GooglePageInfo) page, HttpHostExt.NO_PROXY)));

                GoogleImageExtractor.this.bookContext.imgExecutor.terminate(10L, TimeUnit.MINUTES);

                GoogleImageExtractor.this.logger.info(GoogleImageExtractor.this.bookContext.getBookInfo().getPages().getMissingPagesList());

                long pagesAfter = GoogleImageExtractor.this.bookContext.getPagesStream().filter(pageInfo -> pageInfo.isDataProcessed()).count();

                GoogleImageExtractor.this.bookContext.setPagesProcessed(pagesAfter - GoogleImageExtractor.this.bookContext.getPagesBefore());
                GoogleImageExtractor.this.logger.info(String.format("Processed %s pages", GoogleImageExtractor.this.bookContext.getPagesProcessed()));

                ExecutionContext.INSTANCE.postProcessBook(GoogleImageExtractor.this.bookContext);
            }
        }
    }
}
