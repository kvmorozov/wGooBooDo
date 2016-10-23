package ru.simpleGBD.App.Logic.extractors;

import org.apache.commons.io.IOUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Logic.Proxy.AbstractProxyListProvider;
import ru.simpleGBD.App.Logic.Proxy.HttpHostExt;
import ru.simpleGBD.App.Logic.connectors.Response;
import ru.simpleGBD.App.Logic.model.book.BookData;
import ru.simpleGBD.App.Logic.model.book.BookInfo;
import ru.simpleGBD.App.Logic.model.book.PagesInfo;
import ru.simpleGBD.App.Utils.HttpConnections;
import ru.simpleGBD.App.Utils.Logger;
import ru.simpleGBD.App.Utils.Mapper;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import static ru.simpleGBD.App.Logic.extractors.ImageExtractor.*;

/**
 * Created by km on 08.10.2016.
 */
public class BookInfoExtractor extends AbstractHttpProcessor {

    private static final Logger logger = Logger.getLogger(ExecutionContext.output, ImageExtractor.class.getName());

    private static final String ADD_FLAGS_ATTRIBUTE = "_OC_addFlags";
    private static final String OC_RUN_ATTRIBUTE = "_OC_Run";
    private static final String BOOK_INFO_START_TAG = "fullview";
    private static final String BOOK_INFO_END_TAG = "enableUserFeedbackUI";
    private static final String OPEN_PAGE_ADD_URL = "&printsec=frontcover&hl=ru#v=onepage&q&f=false";

    private BookInfo bookInfo;
    private String bookUrl;

    public BookInfoExtractor() {
        bookUrl = HTTPS_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, ExecutionContext.bookId) + OPEN_PAGE_ADD_URL;
        findBookInfo();
    }

    private void findBookInfo() {
        Document defaultDocument = getDocumentWithoutProxy();
        try {
            BookInfo defaultBookInfo = extractBookInfo(defaultDocument);
            if (defaultBookInfo == null) {
                HttpHostExt.NO_PROXY.forceInvalidate();

                Iterator<HttpHostExt> hostIterator = AbstractProxyListProvider.getInstance().getProxyList();
                while (hostIterator.hasNext()) {
                    HttpHostExt proxy = hostIterator.next();
                    if (proxy == null)
                        continue;

                    BookInfo proxyBookInfo = extractBookInfo(getDocumentWithProxy(proxy));
                    if (proxyBookInfo == null)
                        proxy.forceInvalidate();
                    else {
                        bookInfo = proxyBookInfo;
                        break;
                    }
                }
            } else
                bookInfo = defaultBookInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Document getDocumentWithProxy(HttpHostExt proxy) {
        Response resp = getContent(bookUrl, proxy, true);

        if (resp == null)
            return null;
        else {
            try {
                String respStr = IOUtils.toString(resp.getContent(), Charset.defaultCharset());
                return Jsoup.parse(respStr);
            } catch (IOException e) {
                return null;
            }
        }
    }

    private Document getDocumentWithoutProxy() {
        Connection.Response res = null;
        Document doc = null;

        try {
            res = Jsoup
                    .connect(bookUrl)
                    .userAgent(HttpConnections.USER_AGENT).followRedirects(false).method(Connection.Method.GET).execute();
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

        try {
            doc = res.parse();
            HttpConnections.setDefaultCookies(res.cookies());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return doc;
    }

    private BookInfo extractBookInfo(Document doc) throws IOException {
        if (doc == null)
            return null;

        Elements scripts = doc.select("script");
        for (Element script : scripts) {
            List<Node> childs = script.childNodes();
            if (childs != null && childs.size() > 0) {
                String data = childs.get(0).attr("data");

                if (data == null || data.length() == 0)
                    return null;

                if (data.startsWith(ADD_FLAGS_ATTRIBUTE) && data.indexOf(OC_RUN_ATTRIBUTE) > 0) {
                    int jsonStart = data.indexOf(OC_RUN_ATTRIBUTE) + OC_RUN_ATTRIBUTE.length() + 1;
                    int jsonEnd = data.lastIndexOf(BOOK_INFO_START_TAG) - 3;

                    if (jsonStart <= 0 || jsonEnd <= 0)
                        return null;

                    String pagesJsonData = data.substring(jsonStart, jsonEnd);
                    PagesInfo pages = Mapper.objectMapper.readValue(pagesJsonData, PagesInfo.class);

                    String bookJsonData = data.substring(data.indexOf(BOOK_INFO_START_TAG) - 2, data.lastIndexOf(BOOK_INFO_END_TAG) - 3);
                    BookData bookData = Mapper.objectMapper.readValue(bookJsonData, BookData.class);

                    return new BookInfo(bookData, pages);
                }
            }
        }

        return null;
    }

    public BookInfo getBookInfo() {
        return bookInfo;
    }
}
