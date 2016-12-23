package ru.kmorozov.gbd.core.logic.extractors.google;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.apache.commons.io.FilenameUtils;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractImageExtractor;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;
import ru.kmorozov.gbd.core.logic.model.book.google.GoogleBookData;
import ru.kmorozov.gbd.core.logic.model.book.google.GooglePageInfo;
import ru.kmorozov.gbd.core.utils.Images;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static ru.kmorozov.gbd.core.logic.context.ExecutionContext.INSTANCE;

/**
 * Created by km on 21.11.2015.
 */
public class GoogleImageExtractor extends AbstractImageExtractor {

    public static final int DEFAULT_PAGE_WIDTH = 1280;
    public static final String HTTP_TEMPLATE = "http://books.google.ru/books?id=%BOOK_ID%";
    public static final String HTTPS_TEMPLATE = "https://books.google.ru/books?id=%BOOK_ID%";
    public static final String HTTPS_IMG_TEMPLATE = "https://books.google.ru/books/content?id=%BOOK_ID%";

    public static final String BOOK_ID_PLACEHOLDER = "%BOOK_ID%";
    public static final String RQ_PG_PLACEHOLDER = "%PG%";
    public static final String RQ_SIG_PLACEHOLDER = "%SIG%";
    public static final String RQ_WIDTH_PLACEHOLDER = "%WIDTH%";

    public static final String PAGES_REQUEST_TEMPLATE = "&lpg=PP1&hl=en&pg=%PG%&jscmd=click3";
    public static final String IMG_REQUEST_TEMPLATE = "&pg=%PG%&img=1&zoom=3&hl=ru&sig=%SIG%&w=%WIDTH%";

    private final AtomicInteger proxyReceived = new AtomicInteger(0);
    private final AtomicBoolean processingStarted = new AtomicBoolean(false);

    public GoogleImageExtractor(BookContext bookContext) {
        super(bookContext);
        logger = INSTANCE.getLogger(GoogleImageExtractor.class, bookContext);
    }

    private void scanDir() {
        int imgWidth = GBDOptions.getImageWidth() == 0 ? GoogleImageExtractor.DEFAULT_PAGE_WIDTH : GBDOptions.getImageWidth();
        Path outputPath = Paths.get(bookContext.getOutputDir().toURI());

        try {
            bookContext.getPagesStream().filter(AbstractPage::isFileExists).forEach(page -> {
                try {
                    if (Files.find(outputPath, 1, (path, basicFileAttributes) -> path.toString().contains("\\" + page.getOrder() + "_" + page.getPid() + "."), FOLLOW_LINKS).count() == 0) {
                        logger.severe(String.format("Page %s not found in directory!", page.getPid()));
                        page.dataProcessed.set(false);
                        page.fileExists.set(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            Files.walk(outputPath).forEach(filePath -> {
                setProgress(bookContext.getProgress().incrementAndProgress());

                if (Images.isImageFile(filePath)) {
                    String fileName = FilenameUtils.getBaseName(filePath.toString());
                    String[] nameParts = fileName.split("_");
                    GooglePageInfo _page = (GooglePageInfo) bookContext.getBookInfo().getPages().getPageByPid(nameParts[1]);
                    int order = Integer.valueOf(nameParts[0]);
                    if (_page == null) {
                        logger.severe(String.format("Page %s not found!", fileName));
                        try {
                            Files.delete(filePath);
                            logger.severe(String.format("Page %s deleted!", fileName));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        try {
                            if (GBDOptions.reloadImages()) {
                                BufferedImage bimg = ImageIO.read(new File(filePath.toString()));
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

                            if (!Images.isValidImage(filePath) || (_page.getOrder() != order && !_page.isGapPage())) {
                                Files.delete(filePath);
                                _page.dataProcessed.set(false);
                                logger.severe(String.format("Page %s deleted!", _page.getPid()));
                            }
                        } catch (IOException e) {
                            // Значит файл с ошибкой
                            (new File(filePath.toString())).delete();
                            _page.dataProcessed.set(false);
                            logger.severe(String.format("Page %s deleted!", _page.getPid()));
                        }

                        _page.fileExists.set(true);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bookContext.getProgress() != null) bookContext.getProgress().finish();
        }

        bookContext.setPagesBefore(bookContext.getPagesStream().filter(AbstractPage::isFileExists).count());
    }

    private void setProgress(int i) {
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
        bookContext.getBookInfo().getPages().build();
        scanDir();
    }

    @Override
    public void newProxyEvent(HttpHostExt proxy) {
        (new Thread(new EventProcessor(proxy))).start();
    }

    private class EventProcessor implements Runnable {

        private HttpHostExt proxy;

        EventProcessor(HttpHostExt proxy) {
            this.proxy = proxy;
        }

        @Override
        public void run() {
            while (!initComplete.get()) {
                waitingProxy.add(proxy);
                return;
            }

            if (proxy.isAvailable()) bookContext.sigExecutor.execute(new GooglePageSigProcessor(bookContext, proxy));

            int proxyNeeded = INSTANCE.getProxyCount() - proxyReceived.incrementAndGet();

            if (proxyNeeded <= 0) {
                if (!processingStarted.compareAndSet(false, true)) return;

                bookContext.sigExecutor.terminate(10, TimeUnit.MINUTES);

                bookContext.getPagesStream().filter(page -> !page.dataProcessed.get() && ((GooglePageInfo) page).getSig() != null).forEach(page -> bookContext.imgExecutor.execute(new GooglePageImgProcessor(bookContext, (GooglePageInfo) page, HttpHostExt.NO_PROXY)));

                bookContext.imgExecutor.terminate(10, TimeUnit.MINUTES);

                logger.info(bookContext.getBookInfo().getPages().getMissingPagesList());

                long pagesAfter = bookContext.getPagesStream().filter(pageInfo -> pageInfo.dataProcessed.get()).count();

                bookContext.setPagesProcessed(pagesAfter - bookContext.getPagesBefore());
                logger.info(String.format("Processed %s pages", bookContext.getPagesProcessed()));

                INSTANCE.postProcessBook(bookContext);
            }
        }
    }
}
