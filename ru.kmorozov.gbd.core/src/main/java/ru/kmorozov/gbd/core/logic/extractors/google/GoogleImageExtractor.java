package ru.kmorozov.gbd.core.logic.extractors.google;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.apache.commons.io.FilenameUtils;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractImageExtractor;
import ru.kmorozov.gbd.core.logic.model.book.google.GogglePageInfo;
import ru.kmorozov.gbd.core.logic.model.book.google.GoogleBookData;
import ru.kmorozov.gbd.core.utils.Images;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.kmorozov.gbd.core.logic.context.ExecutionContext.INSTANCE;

/**
 * Created by km on 21.11.2015.
 */
public class GoogleImageExtractor extends AbstractImageExtractor {

    public static final int DEFAULT_PAGE_WIDTH = 1280;
    public static final String HTTP_TEMPLATE = "http://books.google.ru/books?id=%BOOK_ID%";
    public static final String HTTPS_TEMPLATE = "https://books.google.ru/books?id=%BOOK_ID%";

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
        try {
            Files.walk(Paths.get(bookContext.getOutputDir().toURI())).forEach(filePath -> {
                setProgress(bookContext.getProgress().incrementAndProgress());

                if (Images.isImageFile(filePath)) {
                    String fileName = FilenameUtils.getBaseName(filePath.toString());
                    String[] nameParts = fileName.split("_");
                    GogglePageInfo _page = (GogglePageInfo) bookContext.getBookInfo().getPages().getPageByPid(nameParts[1]);
                    if (_page != null) {
                        try {
                            if (GBDOptions.reloadImages()) {
                                BufferedImage bimg = ImageIO.read(new File(filePath.toString()));
                                _page.setWidth(bimg.getWidth());
                                _page.dataProcessed.set(bimg.getWidth() >= GBDOptions.getImageWidth());

                                // 1.4 - эмпирически, высота переменная
                                if (bimg.getWidth() * 1.4 > bimg.getHeight()) {
                                    (new File(filePath.toString())).delete();
                                    _page.dataProcessed.set(false);
                                    logger.severe(String.format("Page %s deleted!", _page.getPid()));
                                }
                            } else _page.dataProcessed.set(true);
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
    }

    private void setProgress(int i) {
    }

    @Override
    protected boolean preCheck() {
        if (!Strings.isNullOrEmpty(((GoogleBookData) bookContext.getBookInfo().getBookData()).getFlags().getDownloadPdfUrl())) {
            logger.severe("There is direct url to download book. DIY!");
            return false;
        }
        else
            return true;
    }

    @Override
    protected void prepareDirectory() {
        super.prepareDirectory();
        bookContext.getBookInfo().getPages().build();
        scanDir();

        waitingProxy.forEach(this::newProxyEvent);
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

                INSTANCE.updateBlacklist();

                bookContext.sigExecutor.terminate(3, TimeUnit.MINUTES);

                bookContext.getPagesStream()
                        .filter(page -> !page.dataProcessed.get() && ((GogglePageInfo) page).getSig() != null)
                        .forEach(page -> bookContext.imgExecutor.execute(new GooglePageImgProcessor(bookContext, (GogglePageInfo) page, HttpHostExt.NO_PROXY)));

                bookContext.imgExecutor.terminate(5, TimeUnit.MINUTES);

                INSTANCE.updateProxyList();

                logger.info(bookContext.getBookInfo().getPages().getMissingPagesList());

                long pagesAfter = bookContext.getPagesStream().filter(pageInfo -> pageInfo.dataProcessed.get()).count();

                logger.info(String.format("Processed %s pages", pagesAfter - bookContext.getPagesBefore()));

                synchronized (bookContext) {
                    INSTANCE.postProcessBook(bookContext);
                }
            } //else logger.finest(String.format("Waiting for %s more proxy", proxyNeeded));
        }
    }

    @Override
    public void newProxyEvent(HttpHostExt proxy) {
        (new Thread(new EventProcessor(proxy))).start();
    }
}
