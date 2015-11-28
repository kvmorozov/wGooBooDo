package ru.simpleGBD.App.Logic.Runtime;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import ru.simpleGBD.App.Logic.DataModel.BookData;
import ru.simpleGBD.App.Logic.DataModel.BookInfo;
import ru.simpleGBD.App.Logic.DataModel.PageInfo;
import ru.simpleGBD.App.Logic.DataModel.PagesInfo;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Logic.Proxy.IProxyListProvider;
import ru.simpleGBD.App.Utils.HttpConnections;
import ru.simpleGBD.App.Utils.Mapper;
import ru.simpleGBD.App.Utils.Pools;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by km on 21.11.2015.
 */
public class ImageExtractor {

    private static Logger logger = Logger.getLogger(ImageExtractor.class.getName());

    private static final String ADD_FLAGS_ATTRIBUTE = "_OC_addFlags";
    private static final String OC_RUN_ATTRIBUTE = "_OC_Run";
    private static final String BOOK_INFO_START_TAG = "fullview";
    private static final String BOOK_INFO_END_TAG = "enableUserFeedbackUI";

    public static final String RQ_PG_PLACEHOLDER = "%PG%";
    public static final String RQ_SIG_PLACEHOLDER = "%SIG%";
    public static final String RQ_WIDTH_PLACEHOLDER = "%WIDTH%";
    public static final String PAGES_REQUEST_TEMPLATE = "&lpg=PP1&hl=ru&pg=%PG%&jscmd=click3";
    public static final String IMG_REQUEST_TEMPLATE = "&pg=%PG%&img=1&zoom=3&hl=ru&sig=%SIG%&w=%WIDTH%";
    public static final String OPEN_PAGE_ADD_URL = "&printsec=frontcover&hl=ru#v=onepage&q&f=false";

    private static final String OUTPUT_DIR = "C:\\Work\\imgOut";

    public ImageExtractor(String url) {
        ExecutionContext.baseUrl = url;

        if (IProxyListProvider.getInstance().getProxyList() != null && IProxyListProvider.getInstance().getProxyList().size() > 0)
            logger.info(String.format("Starting with %s proxies.", IProxyListProvider.getInstance().getProxyList().size()));
    }

    private BookInfo getBookInfo() throws IOException {
        Connection.Response res = Jsoup
                .connect(ExecutionContext.baseUrl + OPEN_PAGE_ADD_URL)
                .userAgent(HttpConnections.USER_AGENT).method(Connection.Method.GET).execute();

        Document doc = res.parse();
        HttpConnections.INSTANCE.setCookies(res.cookies());

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
        for (PageInfo page : ExecutionContext.bookInfo.getPages().getPagesArray())
            if (page.getSig() == null && page.sigRequestLock.tryLock()) {
                logger.finest(String.format("Starting processing for img = %s", page.getPid()));

                page.sigRequestLock.lock();
                Pools.sigExecutor.execute(new PageSigProcessor(page));
            }

        Pools.sigExecutor.shutdown();
        try {
            Pools.sigExecutor.awaitTermination(1, TimeUnit.MINUTES);
            HttpConnections.INSTANCE.closeAllConnections();
        } catch (InterruptedException e) {
        }
        ExecutionContext.bookInfo.getPages().exportPagesUrls();

        for (PageInfo page : ExecutionContext.bookInfo.getPages().getPagesArray())
            if (page.getSig() != null)
                Pools.imgExecutor.execute(new PageImgProcessor(page));

        Pools.imgExecutor.shutdown();
        try {
            Pools.imgExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }
    }

    public void process() {
        try {
            ExecutionContext.bookInfo = getBookInfo();

            ExecutionContext.outputDir = new File(OUTPUT_DIR + "\\" + ExecutionContext.bookInfo.getBookData().getTitle() + " " + ExecutionContext.bookInfo.getBookData().getVolumeId());
            if (!ExecutionContext.outputDir.exists())
                ExecutionContext.outputDir.mkdir();

            ExecutionContext.bookInfo.getPages().build();
            getPagesInfo();

        } catch (HttpStatusException hse) {
            logger.severe(String.format("Cannot process images: %s (%d)", hse.getMessage(), hse.getStatusCode()));
        } catch (IOException e) {
            logger.severe("Cannot process images!");
        }
    }

    public int getPagesCount() {
        return ExecutionContext.bookInfo.getPages().getPagesCount();
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
