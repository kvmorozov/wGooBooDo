package ru.kmorozov.gbd.core.logic.extractors.google;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.apache.commons.io.FilenameUtils;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractImageExtractor;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;
import ru.kmorozov.gbd.core.logic.model.book.google.GoogleBookData;
import ru.kmorozov.gbd.core.logic.model.book.google.GooglePageInfo;
import ru.kmorozov.gbd.utils.Images;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.kmorozov.gbd.core.config.constants.GoogleConstants.DEFAULT_PAGE_WIDTH;

/**
 * Created by km on 21.11.2015.
 */
public class GoogleImageExtractor extends AbstractImageExtractor {

    private final AtomicInteger proxyReceived = new AtomicInteger(0);
    private final AtomicBoolean processingStarted = new AtomicBoolean(false);

    public GoogleImageExtractor(final BookContext bookContext) {
        super(bookContext, GoogleImageExtractor.class);
    }

    @Override
    protected void scanDir() {
        final int imgWidth = 0 == GBDOptions.getImageWidth() ? DEFAULT_PAGE_WIDTH : GBDOptions.getImageWidth();
        final Path outputPath = Paths.get(bookContext.getOutputDir().toURI());

        try {
            bookContext.getPagesStream().filter(AbstractPage::isFileExists).forEach(page -> {
                try {
                    if (0 == Files.find(outputPath, 1, (path, basicFileAttributes) -> path.toString().contains("\\" + page.getOrder() + '_' + page.getPid() + '.'), FileVisitOption.FOLLOW_LINKS).count()) {
                        logger.severe(String.format("Page %s not found in directory!", page.getPid()));
                        page.dataProcessed.set(false);
                        page.fileExists.set(false);
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            });

            Files.walk(outputPath).forEach(filePath -> {
                setProgress(bookContext.getProgress().incrementAndProgress());

                if (Images.isImageFile(filePath)) {
                    final String fileName = FilenameUtils.getBaseName(filePath.toString());
                    final String[] nameParts = fileName.split("_");
                    final GooglePageInfo _page = (GooglePageInfo) bookContext.getBookInfo().getPages().getPageByPid(nameParts[1]);
                    final int order = Integer.valueOf(nameParts[0]);
                    if (null == _page) {
                        logger.severe(String.format("Page %s not found!", fileName));
                        try {
                            Files.delete(filePath);
                            logger.severe(String.format("Page %s deleted!", fileName));
                        } catch (final IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        try {
                            if (GBDOptions.reloadImages()) {
                                final BufferedImage bimg = ImageIO.read(new File(filePath.toString()));
                                _page.setWidth(bimg.getWidth());
                                _page.dataProcessed.set(bimg.getWidth() >= imgWidth);

                                // 1.4 - эмпирически, высота переменная
                                if (bimg.getWidth() * 1.4 > bimg.getHeight()) {
                                    Files.delete(filePath);
                                    _page.dataProcessed.set(false);
                                    logger.severe(String.format("Page %s deleted!", _page.getPid()));
                                }
                            }
                            else _page.dataProcessed.set(true);

                            if (Images.isInvalidImage(filePath, imgWidth)) {
                                _page.dataProcessed.set(false);
                                Files.delete(filePath);
                                logger.severe(String.format("Page %s deleted!", _page.getPid()));
                            }
                            else if (_page.getOrder() != order && !_page.isGapPage()) {
                                final File oldFile = filePath.toFile();
                                final File newFile = new File(filePath.toString().replace(order + "_", _page.getOrder() + "_"));
                                if (!newFile.exists())
                                    oldFile.renameTo(newFile);
                                _page.dataProcessed.set(true);
                                logger.severe(String.format("Page %s renamed!", _page.getPid()));
                            }
                        } catch (final IOException e) {
                            // Значит файл с ошибкой
                            (new File(filePath.toString())).delete();
                            _page.dataProcessed.set(false);
                            logger.severe(String.format("Page %s deleted!", _page.getPid()));
                        }

                        _page.fileExists.set(true);
                    }
                }
            });
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            if (null != bookContext.getProgress()) bookContext.getProgress().finish();
        }

        bookContext.setPagesBefore(bookContext.getPagesStream().filter(AbstractPage::isFileExists).count());
    }

    private void setProgress(final int i) {
    }

    @Override
    protected boolean preCheck() {
        if (!Strings.isNullOrEmpty(((GoogleBookData) bookContext.getBookInfo().getBookData()).getFlags().getDownloadPdfUrl())) {
            logger.severe("There is direct url to download book. DIY!");
            return false;
        }
        else return true;
    }

    @Override
    protected void prepareDirectory() {
        super.prepareDirectory();
        bookContext.getBookInfo().getPages().build(logger);
    }

    @Override
    public void newProxyEvent(final HttpHostExt proxy) {
        if (null != proxy)
            (new Thread(new EventProcessor(proxy))).start();
    }

    private class EventProcessor implements Runnable {

        private final HttpHostExt proxy;

        EventProcessor(final HttpHostExt proxy) {
            this.proxy = proxy;
        }

        @Override
        public void run() {
            while (!initComplete.get()) {
                waitingProxy.add(proxy);
                return;
            }

            if (proxy.isAvailable()) bookContext.sigExecutor.execute(new GooglePageSigProcessor(bookContext, proxy));

            final int proxyNeeded = ExecutionContext.getProxyCount() - proxyReceived.incrementAndGet();

            if (0 >= proxyNeeded) {
                if (!processingStarted.compareAndSet(false, true)) return;

                bookContext.sigExecutor.terminate(10, TimeUnit.MINUTES);

                bookContext.getPagesStream().filter(page -> !page.dataProcessed.get() && null != ((GooglePageInfo) page).getSig()).forEach(page -> bookContext.imgExecutor.execute(new GooglePageImgProcessor(bookContext, (GooglePageInfo) page, HttpHostExt.NO_PROXY)));

                bookContext.imgExecutor.terminate(10, TimeUnit.MINUTES);

                logger.info(bookContext.getBookInfo().getPages().getMissingPagesList());

                final long pagesAfter = bookContext.getPagesStream().filter(pageInfo -> pageInfo.dataProcessed.get()).count();

                bookContext.setPagesProcessed(pagesAfter - bookContext.getPagesBefore());
                logger.info(String.format("Processed %s pages", bookContext.getPagesProcessed()));

                ExecutionContext.INSTANCE.postProcessBook(bookContext);
            }
        }
    }
}
