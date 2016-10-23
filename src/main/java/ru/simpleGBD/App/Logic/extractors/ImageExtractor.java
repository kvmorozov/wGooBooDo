package ru.simpleGBD.App.Logic.extractors;

import org.apache.commons.io.FilenameUtils;
import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Logic.ExecutionContext;
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
import java.util.concurrent.TimeUnit;

/**
 * Created by km on 21.11.2015.
 */
public class ImageExtractor extends AbstractEventSource {

    private static final Logger logger = Logger.getLogger(ExecutionContext.output, ImageExtractor.class.getName());

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
        ExecutionContext.bookId = GBDOptions.getBookId();
        ExecutionContext.baseUrl = HTTPS_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, ExecutionContext.bookId);

        this.output = ExecutionContext.output;
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

        ExecutionContext.bookInfo.getPagesInfo().getPages().stream().filter(page -> !page.dataProcessed.get() && page.getSig() != null).forEach(page -> Pools.imgExecutor.execute(new PageImgProcessor(page, HttpHostExt.NO_PROXY)));

        Pools.imgExecutor.shutdown();
        try {
            Pools.imgExecutor.awaitTermination(500, TimeUnit.MINUTES);
        } catch (InterruptedException ignored) {
        }

        AbstractProxyListProvider.getInstance().updateProxyList();
    }

    private void scanDir() {
        final ProcessStatus psScan = new ProcessStatus(ExecutionContext.outputDir.listFiles().length);
        setProcessStatus(psScan);

        try {
            Files.walk(Paths.get(ExecutionContext.outputDir.toURI())).forEach(filePath -> {
                setProgress(psScan.incrementAndProgress());

                if (Images.isImageFile(filePath)) {
                    String fileName = FilenameUtils.getBaseName(filePath.toString());
                    String[] nameParts = fileName.split("_");
                    PageInfo _page = ExecutionContext.bookInfo.getPagesInfo().getPageByPid(nameParts[1]);
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
        ExecutionContext.bookInfo = (new BookInfoExtractor()).getBookInfo();

        if (ExecutionContext.bookInfo == null) throw new RuntimeException("No book info!");

        output.receiveBookInfo(ExecutionContext.bookInfo);

        String baseOutputDirPath = GBDOptions.getOutputDir();
        if (baseOutputDirPath == null) return;

        File baseOutputDir = new File(baseOutputDirPath);
        if (!baseOutputDir.exists()) baseOutputDir.mkdir();

        logger.info(String.format("Working with %s", ExecutionContext.bookInfo.getBookData().getTitle()));

        ExecutionContext.outputDir = new File(baseOutputDirPath + "\\" + ExecutionContext.bookInfo.getBookData().getTitle().replace(":", "").replace("<", "").replace(">", "").replace("/", ".") + " " + ExecutionContext.bookInfo.getBookData().getVolumeId());
        if (!ExecutionContext.outputDir.exists()) {
            boolean dirResult = ExecutionContext.outputDir.mkdir();
            if (!dirResult) {
                logger.severe(String.format("Invalid book title: %s", ExecutionContext.bookInfo.getBookData().getTitle()));
                return;
            }
        }

        ExecutionContext.bookInfo.getPagesInfo().build();
        scanDir();

        getPagesInfo();
        logger.info(ExecutionContext.bookInfo.getPagesInfo().getMissingPagesList());

        PdfMaker pdfMaker = new PdfMaker(ExecutionContext.outputDir, ExecutionContext.bookInfo);
        pdfMaker.make();
    }
}
