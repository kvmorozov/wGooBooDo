package ru.simpleGBD.App.Logic.Runtime;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import ru.simpleGBD.App.Logic.DataModel.PageInfo;
import ru.simpleGBD.App.Logic.DataModel.PagesInfo;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Utils.HttpConnections;
import ru.simpleGBD.App.Utils.Mapper;
import ru.simpleGBD.App.Utils.Pools;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by km on 21.11.2015.
 */
public class ImageExtractor {

    private static Logger logger = Logger.getLogger(ImageExtractor.class.getName());

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.143 Safari/537.36";
    private static final String ADD_FLAGS_ATTRIBUTE = "_OC_addFlags";
    private static final String OC_RUN_ATTRIBUTE = "_OC_Run";
    private static final String BOOK_INFO_START_TAG = "fullview";

    public static final String RQ_PG_PLACEHOLDER = "%PG%";
    public static final String RQ_SIG_PLACEHOLDER = "%SIG%";
    public static final String RQ_WIDTH_PLACEHOLDER = "%WIDTH%";
    public static final String PAGES_REQUEST_TEMPLATE = "&lpg=PP1&hl=ru&pg=%PG%&jscmd=click3";
    public static final String IMG_REQUEST_TEMPLATE = "&pg=%PG%&img=1&zoom=3&hl=ru&sig=%SIG%&w=%WIDTH%";
    public static final String OPEN_PAGE_ADD_URL = "&printsec=frontcover&hl=ru#v=onepage&q&f=false";

    private static final String OUTPUT_DIR = "C:\\Work\\imgOut";


    public ImageExtractor(String url) {
        ExecutionContext.baseUrl = url;

        ExecutionContext.outputDir = new File(OUTPUT_DIR + "\\" + System.currentTimeMillis());
        ExecutionContext.outputDir.mkdir();
    }

    private PagesInfo getBookInfo() throws IOException {
        Connection.Response res = Jsoup
                .connect(ExecutionContext.baseUrl + OPEN_PAGE_ADD_URL)
                .userAgent(USER_AGENT).method(Connection.Method.GET).execute();

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
                    String jsonData = data.substring(data.indexOf(OC_RUN_ATTRIBUTE) + OC_RUN_ATTRIBUTE.length() + 1, data.lastIndexOf(BOOK_INFO_START_TAG) - 3);

                    return Mapper.objectMapper.readValue(jsonData, PagesInfo.class);
                }
            }
        }

        return null;
    }

    private void getPagesInfo() throws IOException {
        for (PageInfo page : ExecutionContext.bookInfo.getPages())
            if (page.getSig() == null && page.sigRequestLock.tryLock()) {
                logger.finest(String.format("Starting processing for img = %s", page.getPid()));

                page.sigRequestLock.lock();
                Pools.sigExecutor.execute(new PageSigProcessor(page));
            }

        Pools.sigExecutor.shutdown();
        try {
            Pools.sigExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }
        ExecutionContext.bookInfo.exportPagesUrls();

        for (PageInfo page : ExecutionContext.bookInfo.getPages())
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
            ExecutionContext.bookInfo.build();
            getPagesInfo();

        } catch (HttpStatusException hse) {
            logger.severe(String.format("Cannot process images: %s (%d)", hse.getMessage(), hse.getStatusCode()));
        } catch (IOException e) {
            logger.severe("Cannot process images!");
        }
    }

    public int getPagesCount() {
        return ExecutionContext.bookInfo.getPagesCount();
    }

    public boolean validate() {
        try {
            URL bookUrl = new URL(ExecutionContext.baseUrl);
            URLConnection connection = bookUrl.openConnection();

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
