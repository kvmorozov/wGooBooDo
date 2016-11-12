package ru.kmorozov.gbd.core.logic.extractors;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.apache.commons.io.FilenameUtils;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.AbstractProxyListProvider;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.model.book.PageInfo;
import ru.kmorozov.gbd.core.logic.output.consumers.AbstractOutput;
import ru.kmorozov.gbd.core.logic.output.events.AbstractEventSource;
import ru.kmorozov.gbd.core.utils.Images;
import ru.kmorozov.gbd.core.utils.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.kmorozov.gbd.core.logic.context.ExecutionContext.INSTANCE;

/**
 * Created by km on 21.11.2015.
 */
public class ImageExtractor extends AbstractEventSource implements Runnable {

    private final Logger logger;

    public static final int DEFAULT_PAGE_WIDTH = 1280;
    public static final String HTTP_TEMPLATE = "http://books.google.ru/books?id=%BOOK_ID%";
    public static final String HTTPS_TEMPLATE = "https://books.google.ru/books?id=%BOOK_ID%";

    public static final String BOOK_ID_PLACEHOLDER = "%BOOK_ID%";
    public static final String RQ_PG_PLACEHOLDER = "%PG%";
    public static final String RQ_SIG_PLACEHOLDER = "%SIG%";
    public static final String RQ_WIDTH_PLACEHOLDER = "%WIDTH%";

    public static final String PAGES_REQUEST_TEMPLATE = "&lpg=PP1&hl=en&pg=%PG%&jscmd=click3";
    public static final String IMG_REQUEST_TEMPLATE = "&pg=%PG%&img=1&zoom=3&hl=ru&sig=%SIG%&w=%WIDTH%";

    private final AbstractOutput output;
    private final BookContext bookContext;

    private final AtomicInteger proxyReceived = new AtomicInteger(0);
    private final AtomicBoolean initComplete = new AtomicBoolean(false);
    private long pagesBefore = 0l;

    public ImageExtractor(BookContext bookContext) {
        setProcessStatus(bookContext.getProgress());
        this.bookContext = bookContext;

        logger = INSTANCE.getLogger(ImageExtractor.class, bookContext);
        this.output = INSTANCE.getOutput();
        bookContext.setExtractor(this);
    }

    private void scanDir() {
        try {
            Files.walk(Paths.get(bookContext.getOutputDir().toURI())).forEach(filePath -> {
                setProgress(bookContext.getProgress().incrementAndProgress());

                if (Images.isImageFile(filePath)) {
                    String fileName = FilenameUtils.getBaseName(filePath.toString());
                    String[] nameParts = fileName.split("_");
                    PageInfo _page = bookContext.getBookInfo().getPagesInfo().getPageByPid(nameParts[1]);
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
    public void run() {
        process();
    }

    public void process() {
        if (!Strings.isNullOrEmpty(bookContext.getBookInfo().getBookData().getFlags().getDownloadPdfUrl())) {
            logger.severe("There is direct url to download book. DIY!");
            return;
        }

        output.receiveBookInfo(Objects.requireNonNull(bookContext.getBookInfo()));

        String baseOutputDirPath = GBDOptions.getOutputDir();
        if (baseOutputDirPath == null) return;

        File baseOutputDir = new File(baseOutputDirPath);
        if (!baseOutputDir.exists()) if (!baseOutputDir.mkdir()) return;

        logger.info(String.format("Working with %s", bookContext.getBookInfo().getBookData().getTitle()));

        bookContext.setOutputDir(new File(baseOutputDirPath + "\\" + bookContext.getBookInfo().getBookData().getTitle().replace(":", "").replace("<", "").replace(">", "").replace("/", ".") + " " + bookContext.getBookInfo().getBookData().getVolumeId()));
        File[] files = bookContext.getOutputDir().listFiles();
        bookContext.getProgress().resetMaxValue(files == null ? 0 : files.length);

        if (!bookContext.getOutputDir().exists()) {
            boolean dirResult = bookContext.getOutputDir().mkdir();
            if (!dirResult) {
                logger.severe(String.format("Invalid book title: %s", bookContext.getBookInfo().getBookData().getTitle()));
                return;
            }
        }

        bookContext.getBookInfo().getPagesInfo().build();
        scanDir();

        pagesBefore = bookContext.getBookInfo().getPagesInfo().getPages().stream().filter(pageInfo -> pageInfo.dataProcessed.get()).count();

        initComplete.set(true);
    }

    public void newProxyEvent(HttpHostExt proxy) {
        while (!initComplete.get()) {
            try {
                Thread.sleep(500l);
            } catch (InterruptedException e) {
                logger.severe(e.getMessage());
            }
        }

        bookContext.sigExecutor.execute(new PageSigProcessor(bookContext, proxy));
        if (proxyReceived.incrementAndGet() >= INSTANCE.getProxyCount()) {
            synchronized (this) {
                bookContext.sigExecutor.shutdown();
                try {
                    bookContext.sigExecutor.awaitTermination(100, TimeUnit.MINUTES);
                } catch (InterruptedException ignored) {
                }

                bookContext.getBookInfo().getPagesInfo().getPages().stream().filter(page -> !page.dataProcessed.get() && page.getSig() != null).forEach(page -> bookContext.imgExecutor.execute(new PageImgProcessor(bookContext, page, HttpHostExt.NO_PROXY)));

                bookContext.imgExecutor.shutdown();
                try {
                    bookContext.imgExecutor.awaitTermination(500, TimeUnit.MINUTES);
                } catch (InterruptedException ignored) {
                }

                INSTANCE.updateProxyList();

                logger.info(bookContext.getBookInfo().getPagesInfo().getMissingPagesList());

                long pagesAfter = bookContext.getBookInfo().getPagesInfo().getPages().stream().filter(pageInfo -> pageInfo.dataProcessed.get()).count();

                logger.info(String.format("Processed %s pages", pagesAfter - pagesBefore));

                AbstractProxyListProvider.updateBlacklist();

                bookContext.getPostProcessor().make(bookContext);
            }
        }
    }
}
