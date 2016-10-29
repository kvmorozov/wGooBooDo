package ru.simpleGBD.App.Logic.extractors;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.apache.commons.io.FilenameUtils;
import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Logic.Output.consumers.AbstractOutput;
import ru.simpleGBD.App.Logic.Output.events.AbstractEventSource;
import ru.simpleGBD.App.Logic.Output.progress.ProcessStatus;
import ru.simpleGBD.App.Logic.Proxy.AbstractProxyListProvider;
import ru.simpleGBD.App.Logic.Proxy.HttpHostExt;
import ru.simpleGBD.App.Logic.model.book.PageInfo;
import ru.simpleGBD.App.Utils.Images;
import ru.simpleGBD.App.Utils.Logger;
import ru.simpleGBD.App.Utils.Pools;
import ru.simpleGBD.App.pdf.PdfMaker;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static ru.simpleGBD.App.Logic.ExecutionContext.INSTANCE;

/**
 * Created by km on 21.11.2015.
 */
public class ImageExtractor extends AbstractEventSource {

    private static final Logger logger = Logger.getLogger(INSTANCE.getOutput(), ImageExtractor.class.getName());

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

    public ImageExtractor() {
        INSTANCE.setBookId(GBDOptions.getBookId());
        INSTANCE.setBaseUrl(HTTPS_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, INSTANCE.getBookId()));

        this.output = INSTANCE.getOutput();
    }

    @Override
    protected Void doInBackground() throws Exception {
        process();

        return null;
    }

    private void getPagesInfo() {
        // Сначала идём без проксм
        Pools.sigExecutor.execute(new PageSigProcessor(HttpHostExt.NO_PROXY));
        // Потом с прокси
        Iterator<HttpHostExt> hostIterator = AbstractProxyListProvider.getInstance().getProxyList();
        while (hostIterator.hasNext()) {
            HttpHostExt proxy = hostIterator.next();
            if (proxy != null) Pools.sigExecutor.execute(new PageSigProcessor(proxy));
        }

        Pools.sigExecutor.shutdown();
        try {
            Pools.sigExecutor.awaitTermination(100, TimeUnit.MINUTES);
        } catch (InterruptedException ignored) {
        }

        INSTANCE.getBookInfo().getPagesInfo().getPages().stream().filter(page -> !page.dataProcessed.get() && page.getSig() != null).forEach(page -> Pools.imgExecutor.execute(new PageImgProcessor(page, HttpHostExt.NO_PROXY)));

        Pools.imgExecutor.shutdown();
        try {
            Pools.imgExecutor.awaitTermination(500, TimeUnit.MINUTES);
        } catch (InterruptedException ignored) {
        }

        AbstractProxyListProvider.getInstance().updateProxyList();
    }

    private void scanDir() {
        final ProcessStatus psScan = new ProcessStatus(INSTANCE.getOutputDir().listFiles().length);
        setProcessStatus(psScan);

        try {
            Files.walk(Paths.get(INSTANCE.getOutputDir().toURI())).forEach(filePath -> {
                setProgress(psScan.incrementAndProgress());

                if (Images.isImageFile(filePath)) {
                    String fileName = FilenameUtils.getBaseName(filePath.toString());
                    String[] nameParts = fileName.split("_");
                    PageInfo _page = INSTANCE.getBookInfo().getPagesInfo().getPageByPid(nameParts[1]);
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
            if (psScan != null) psScan.finish();
        }
    }

    public void process() {
        INSTANCE.setBookInfo((new BookInfoExtractor()).getBookInfo());

        if (!Strings.isNullOrEmpty(INSTANCE.getBookInfo().getBookData().getFlags().getDownloadPdfUrl()))
            throw new RuntimeException("There is direct url to download book. DIY!");

        output.receiveBookInfo(Objects.requireNonNull(INSTANCE.getBookInfo()));

        String baseOutputDirPath = GBDOptions.getOutputDir();
        if (baseOutputDirPath == null) return;

        File baseOutputDir = new File(baseOutputDirPath);
        if (!baseOutputDir.exists()) if (!baseOutputDir.mkdir()) return;

        logger.info(String.format("Working with %s", INSTANCE.getBookInfo().getBookData().getTitle()));

        INSTANCE.setOutputDir(new File(baseOutputDirPath + "\\" + INSTANCE.getBookInfo().getBookData().getTitle().replace(":", "").replace("<", "").replace(">", "").replace("/", ".") + " " + INSTANCE.getBookInfo().getBookData().getVolumeId()));
        if (!INSTANCE.getOutputDir().exists()) {
            boolean dirResult = INSTANCE.getOutputDir().mkdir();
            if (!dirResult) {
                logger.severe(String.format("Invalid book title: %s", INSTANCE.getBookInfo().getBookData().getTitle()));
                return;
            }
        }

        INSTANCE.getBookInfo().getPagesInfo().build();
        scanDir();

        long pagesBefore = INSTANCE.getBookInfo().getPagesInfo().getPages().stream().filter(pageInfo -> pageInfo.dataProcessed.get()).count();

        getPagesInfo();
        logger.info(INSTANCE.getBookInfo().getPagesInfo().getMissingPagesList());

        long pagesAfter = INSTANCE.getBookInfo().getPagesInfo().getPages().stream().filter(pageInfo -> pageInfo.dataProcessed.get()).count();

        logger.info(String.format("Processed %s pages", pagesAfter - pagesBefore));

        PdfMaker pdfMaker = new PdfMaker(INSTANCE.getOutputDir(), INSTANCE.getBookInfo());
        pdfMaker.make(pagesAfter > pagesBefore);
    }
}
