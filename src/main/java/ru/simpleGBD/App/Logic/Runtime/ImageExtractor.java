package ru.simpleGBD.App.Logic.Runtime;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Logic.DataModel.BookData;
import ru.simpleGBD.App.Logic.DataModel.BookInfo;
import ru.simpleGBD.App.Logic.DataModel.PageInfo;
import ru.simpleGBD.App.Logic.DataModel.PagesInfo;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Logic.Proxy.AbstractProxyPistProvider;
import ru.simpleGBD.App.Logic.Proxy.HttpHostExt;
import ru.simpleGBD.App.Utils.HttpConnections;
import ru.simpleGBD.App.Utils.Mapper;
import ru.simpleGBD.App.Utils.Pools;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by km on 21.11.2015.
 */
public class ImageExtractor {

    private static Logger logger = Logger.getLogger(ImageExtractor.class.getName());

    public static final int DEFAULT_PAGE_WIDTH = 800;
    public static final String HTTP_TEMPLATE = "http://74.125.226.3/books?id=%BOOK_ID%";
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

    public ImageExtractor() {
        ExecutionContext.bookId = GBDOptions.getBookId();
        ExecutionContext.baseUrl = HTTP_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, ExecutionContext.bookId);

        List<HttpHostExt> proxyList = AbstractProxyPistProvider.getInstance().getProxyList();
        if (proxyList != null && proxyList.size() > 0)
            logger.info(String.format("Starting with %s proxies.", proxyList.size()));
    }

    private BookInfo getBookInfo() throws IOException {
        Connection.Response res = Jsoup
                .connect(ExecutionContext.baseUrl + OPEN_PAGE_ADD_URL)
                .userAgent(HttpConnections.USER_AGENT).method(Connection.Method.GET).execute();

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
        Pools.sigExecutor.execute(new PageSigProcessor(null));
        // Потом с проксм
        for (HttpHostExt proxy : AbstractProxyPistProvider.getInstance().getProxyList())
            if (proxy.isAvailable() && proxy.getHost().getPort() > 0)
                Pools.sigExecutor.execute(new PageSigProcessor(proxy));

        Pools.sigExecutor.shutdown();
        try {
            Pools.sigExecutor.awaitTermination(100, TimeUnit.MINUTES);
            HttpConnections.INSTANCE.closeAllConnections();
        } catch (InterruptedException e) {
        }

        for (PageInfo page : ExecutionContext.bookInfo.getPagesInfo().getPages())
            if (!page.dataProcessed.get() && page.getSig() != null)
                Pools.imgExecutor.execute(new PageImgProcessor(page, null));

        Pools.imgExecutor.shutdown();
        try {
            Pools.imgExecutor.awaitTermination(500, TimeUnit.MINUTES);
            HttpConnections.INSTANCE.closeAllConnections();
        } catch (InterruptedException e) {
        }
    }

    private void scanDir() {
        try {
            Files.walk(Paths.get(ExecutionContext.outputDir.toURI())).forEach(filePath -> {
                if (Files.isRegularFile(filePath) && FilenameUtils.getExtension(filePath.toString()).equals("png")) {
                    String fileName = FilenameUtils.getBaseName(filePath.toString());
                    String[] nameParts = fileName.split("_");
                    PageInfo _page = ExecutionContext.bookInfo.getPagesInfo().getPageByPid(nameParts[1]);
                    if (_page != null)
                        _page.dataProcessed.set(true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void process() {
        try {
            ExecutionContext.bookInfo = getBookInfo();

            String baseOutputDirPath = GBDOptions.getOutputDir();
            if (baseOutputDirPath == null)
                return;

            File baseOutputDir = new File(baseOutputDirPath);
            if (!baseOutputDir.exists())
                baseOutputDir.mkdir();

            ExecutionContext.outputDir =
                    new File(baseOutputDirPath + "\\" +
                            ExecutionContext.bookInfo.getBookData().getTitle().replace(":", "") +
                            " " + ExecutionContext.bookInfo.getBookData().getVolumeId());
            if (!ExecutionContext.outputDir.exists())
                ExecutionContext.outputDir.mkdir();

            ExecutionContext.bookInfo.getPagesInfo().build();
            scanDir();

            getPagesInfo();

        } catch (HttpStatusException hse) {
            logger.severe(String.format("Cannot process images: %s (%d)", hse.getMessage(), hse.getStatusCode()));
        } catch (IOException e) {
            logger.severe("Cannot process images!");
        }
    }

    public int getPagesCount() {
        return ExecutionContext.bookInfo.getPagesInfo().getPagesCount();
    }

    public boolean validate() {
        try {
            URL bookUrl = new URL(ExecutionContext.baseUrl);
            bookUrl.openConnection();

            return true;
        } catch (MalformedURLException e) {
            logger.severe("Invalid address!");
            return false;
        } catch (IOException e) {
            logger.severe("Cannot open url!");
            return false;
        }
    }
}
