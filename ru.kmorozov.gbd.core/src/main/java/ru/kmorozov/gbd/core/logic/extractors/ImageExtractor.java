package ru.kmorozov.gbd.core.logic.extractors;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.apache.commons.io.FilenameUtils;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.AbstractProxyListProvider;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.model.book.PageInfo;
import ru.kmorozov.gbd.core.logic.output.consumers.AbstractOutput;
import ru.kmorozov.gbd.core.logic.output.events.AbstractEventSource;
import ru.kmorozov.gbd.core.logic.progress.IProgress;
import ru.kmorozov.gbd.core.utils.Images;
import ru.kmorozov.gbd.core.utils.Logger;
import ru.kmorozov.gbd.core.utils.Pools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static ru.kmorozov.gbd.core.logic.ExecutionContext.INSTANCE;

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
    private final IProgress psScan;

    public ImageExtractor(IProgress psScan) {
        this.psScan = psScan;
        setProcessStatus(psScan);

        this.output = INSTANCE.getOutput();
    }

    @Override
    protected Void doInBackground() throws Exception {
        process();

        return null;
    }

    private void getPagesInfo() {
        // Сначала идём без проксм
        Pools.sigExecutor.execute(new PageSigProcessor(HttpHostExt.NO_PROXY, psScan));
        // Потом с прокси
        Iterator<HttpHostExt> hostIterator = AbstractProxyListProvider.getInstance().getProxyList();
        while (hostIterator.hasNext()) {
            HttpHostExt proxy = hostIterator.next();
            if (proxy != null) Pools.sigExecutor.execute(new PageSigProcessor(proxy, psScan));
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

    public long process() {
        INSTANCE.setBookInfo((new BookInfoExtractor()).getBookInfo());

        if (!Strings.isNullOrEmpty(INSTANCE.getBookInfo().getBookData().getFlags().getDownloadPdfUrl()))
            throw new RuntimeException("There is direct url to download book. DIY!");

        output.receiveBookInfo(Objects.requireNonNull(INSTANCE.getBookInfo()));

        String baseOutputDirPath = GBDOptions.getOutputDir();
        if (baseOutputDirPath == null) return 0l;

        File baseOutputDir = new File(baseOutputDirPath);
        if (!baseOutputDir.exists()) if (!baseOutputDir.mkdir()) return 0l;

        logger.info(String.format("Working with %s", INSTANCE.getBookInfo().getBookData().getTitle()));

        INSTANCE.setOutputDir(new File(baseOutputDirPath + "\\" + INSTANCE.getBookInfo().getBookData().getTitle().replace(":", "").replace("<", "").replace(">", "").replace("/", ".") + " " + INSTANCE.getBookInfo().getBookData().getVolumeId()));
        psScan.resetMaxValue(INSTANCE.getOutputDir().listFiles().length);

        if (!INSTANCE.getOutputDir().exists()) {
            boolean dirResult = INSTANCE.getOutputDir().mkdir();
            if (!dirResult) {
                logger.severe(String.format("Invalid book title: %s", INSTANCE.getBookInfo().getBookData().getTitle()));
                return 0l;
            }
        }

        INSTANCE.getBookInfo().getPagesInfo().build();
        scanDir();

        long pagesBefore = INSTANCE.getBookInfo().getPagesInfo().getPages().stream().filter(pageInfo -> pageInfo.dataProcessed.get()).count();

        getPagesInfo();
        logger.info(INSTANCE.getBookInfo().getPagesInfo().getMissingPagesList());

        long pagesAfter = INSTANCE.getBookInfo().getPagesInfo().getPages().stream().filter(pageInfo -> pageInfo.dataProcessed.get()).count();

        logger.info(String.format("Processed %s pages", pagesAfter - pagesBefore));

        AbstractProxyListProvider.updateBlacklist();

        return pagesAfter - pagesBefore;
    }
}
