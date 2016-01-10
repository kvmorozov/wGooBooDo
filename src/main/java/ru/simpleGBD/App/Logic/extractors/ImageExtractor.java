package ru.simpleGBD.App.Logic.extractors;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Logic.Output.consumers.AbstractOutput;
import ru.simpleGBD.App.Logic.Output.events.AbstractEventSource;
import ru.simpleGBD.App.Logic.Output.progress.ProcessStatus;
import ru.simpleGBD.App.Logic.Proxy.AbstractProxyListProvider;
import ru.simpleGBD.App.Logic.Proxy.HttpHostExt;
import ru.simpleGBD.App.Logic.model.book.BookData;
import ru.simpleGBD.App.Logic.model.book.BookInfo;
import ru.simpleGBD.App.Logic.model.book.PageInfo;
import ru.simpleGBD.App.Logic.model.book.PagesInfo;
import ru.simpleGBD.App.Utils.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by km on 21.11.2015.
 */
public class ImageExtractor extends AbstractEventSource {

    private static Logger logger = Logger.getLogger(ExecutionContext.output, ImageExtractor.class.getName());

    public static final int DEFAULT_PAGE_WIDTH = 1280;
    public static final String HTTP_TEMPLATE = "http://books.google.ru/books?id=%BOOK_ID%";
    public static final String HTTPS_TEMPLATE = "https://books.google.ru/books?id=%BOOK_ID%";

    private static final String ADD_FLAGS_ATTRIBUTE = "_OC_addFlags";
    private static final String OC_RUN_ATTRIBUTE = "_OC_Run";
    private static final String BOOK_INFO_START_TAG = "fullview";
    private static final String BOOK_INFO_END_TAG = "enableUserFeedbackUI";

    public static final String BOOK_ID_PLACEHOLDER = "%BOOK_ID%";
    public static final String RQ_PG_PLACEHOLDER = "%PG%";
    public static final String RQ_SIG_PLACEHOLDER = "%SIG%";
    public static final String RQ_WIDTH_PLACEHOLDER = "%WIDTH%";

    public static final String PAGES_REQUEST_TEMPLATE = "&lpg=PP1&hl=ru&pg=%PG%&jscmd=click3";
    public static final String IMG_REQUEST_TEMPLATE = "&pg=%PG%&img=1&zoom=3&hl=ru&sig=%SIG%&w=%WIDTH%";
    public static final String OPEN_PAGE_ADD_URL = "&printsec=frontcover&hl=ru#v=onepage&q&f=false";

    private AbstractOutput output;

    public ImageExtractor() {
        ExecutionContext.bookId = GBDOptions.getBookId();
        ExecutionContext.baseUrl = HTTPS_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, ExecutionContext.bookId);

        this.output = ExecutionContext.output;

        List<HttpHostExt> proxyList = AbstractProxyListProvider.getInstance().getProxyList();
        if (proxyList != null && proxyList.size() > 0)
            logger.info(String.format("Starting with %s proxies.", proxyList.size()));
    }

    @Override
    protected Void doInBackground() throws Exception {
        process();

        return null;
    }

    private BookInfo getBookInfo() throws IOException {
        Connection.Response res = null;

        try {
            res = Jsoup
                    .connect(HTTPS_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, ExecutionContext.bookId) + OPEN_PAGE_ADD_URL)
                    .userAgent(HttpConnections.USER_AGENT).method(Connection.Method.GET).execute();
        } catch (UnknownHostException uhe) {
            logger.severe("Not connected to Internet!");
        } catch (Exception ex) {
            try {
                res = Jsoup
                        .connect(HTTP_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, ExecutionContext.bookId) + OPEN_PAGE_ADD_URL)
                        .userAgent(HttpConnections.USER_AGENT).method(Connection.Method.GET).execute();
            } catch (Exception ex1) {
                throw new RuntimeException(ex1);
            }
        }

        Document doc = res.parse();
        HttpConnections.INSTANCE.setDefaultCookies(res.cookies());

        Elements scripts = doc.select("script");
        for (Element script : scripts) {
            List<Node> childs = script.childNodes();
            if (childs != null && childs.size() > 0) {
                String data = childs.get(0).attr("data");

                if (data == null || data.length() == 0)
                    return null;

                if (data.startsWith(ADD_FLAGS_ATTRIBUTE) && data.indexOf(OC_RUN_ATTRIBUTE) > 0) {
                    String pagesJsonData = data.substring(data.indexOf(OC_RUN_ATTRIBUTE) + OC_RUN_ATTRIBUTE.length() + 1, data.lastIndexOf(BOOK_INFO_START_TAG) - 3);
                    PagesInfo pages = Mapper.objectMapper.readValue(pagesJsonData, PagesInfo.class);

                    String bookJsonData = data.substring(data.indexOf(BOOK_INFO_START_TAG) - 2, data.lastIndexOf(BOOK_INFO_END_TAG) - 3);
                    BookData bookData = Mapper.objectMapper.readValue(bookJsonData, BookData.class);

                    return new BookInfo(bookData, pages);
                }
            }
        }

        return null;
    }

    private void getPagesInfo() throws IOException {
        // Сначала идём без проксм
        Pools.sigExecutor.execute(new PageSigProcessor(HttpHostExt.NO_PROXY));
        // Потом с прокси
        for (HttpHostExt proxy : AbstractProxyListProvider.getInstance().getProxyList())
            if (proxy.isAvailable() && proxy.getHost().getPort() > 0)
                Pools.sigExecutor.execute(new PageSigProcessor(proxy));

        Pools.sigExecutor.shutdown();
        try {
            Pools.sigExecutor.awaitTermination(100, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
        }

        for (PageInfo page : ExecutionContext.bookInfo.getPagesInfo().getPages())
            if (!page.dataProcessed.get() && page.getSig() != null)
                Pools.imgExecutor.execute(new PageImgProcessor(page, null));

        Pools.imgExecutor.shutdown();
        try {
            Pools.imgExecutor.awaitTermination(500, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
        }
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
                            } else
                                _page.dataProcessed.set(true);
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
            if (psScan != null)
                psScan.finish();
        }
    }

    public void process() {
        try {
            ExecutionContext.bookInfo = getBookInfo();
            output.receiveBookInfo(ExecutionContext.bookInfo);

            String baseOutputDirPath = GBDOptions.getOutputDir();
            if (baseOutputDirPath == null)
                return;

            File baseOutputDir = new File(baseOutputDirPath);
            if (!baseOutputDir.exists())
                baseOutputDir.mkdir();

            ExecutionContext.outputDir =
                    new File(baseOutputDirPath + "\\" +
                            ExecutionContext.bookInfo.getBookData().getTitle()
                                    .replace(":", "")
                                    .replace("/", ".") +
                            " " + ExecutionContext.bookInfo.getBookData().getVolumeId());
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
        } catch (HttpStatusException hse) {
            logger.severe(String.format("Cannot process images: %s (%d)", hse.getMessage(), hse.getStatusCode()));
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("Cannot process images!");
        }
    }
}
