package ru.kmorozov.gbd.core.logic.extractors;

import org.apache.commons.io.IOUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import ru.kmorozov.gbd.core.logic.Proxy.AbstractProxyListProvider;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.Response;
import ru.kmorozov.gbd.core.logic.model.book.BookData;
import ru.kmorozov.gbd.core.logic.model.book.BookInfo;
import ru.kmorozov.gbd.core.logic.model.book.PagesInfo;
import ru.kmorozov.gbd.core.utils.HttpConnections;
import ru.kmorozov.gbd.core.utils.Logger;
import ru.kmorozov.gbd.core.utils.Mapper;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import static ru.kmorozov.gbd.core.config.storage.BookContextLoader.BOOK_CTX_LOADER;
import static ru.kmorozov.gbd.core.logic.context.ExecutionContext.INSTANCE;
import static ru.kmorozov.gbd.core.logic.extractors.ImageExtractor.*;

/**
 * Created by km on 08.10.2016.
 */
public class BookInfoExtractor extends AbstractHttpProcessor {

    private static final Logger logger = INSTANCE.getLogger(ImageExtractor.class);

    private static final String ADD_FLAGS_ATTRIBUTE = "_OC_addFlags";
    private static final String OC_RUN_ATTRIBUTE = "_OC_Run";
    private static final String BOOK_INFO_START_TAG = "fullview";
    private static final String BOOK_INFO_END_TAG = "enableUserFeedbackUI";
    private static final String OPEN_PAGE_ADD_URL = "&printsec=frontcover&hl=ru#v=onepage&q&f=false";

    private BookInfo bookInfo;
    private String bookUrl;
    private final String bookId;

    public BookInfoExtractor(String bookId) {
        this.bookId = bookId;
        bookUrl = HTTPS_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, bookId) + OPEN_PAGE_ADD_URL;

        BookInfo storedBookInfo = BOOK_CTX_LOADER.getBookInfo(bookId);
        bookInfo = storedBookInfo == null ? findBookInfo() : storedBookInfo;
    }

    private BookInfo findBookInfo() {
        Document defaultDocument = getDocumentWithoutProxy();
        try {
            BookInfo defaultBookInfo = extractBookInfo(defaultDocument);
            if (defaultBookInfo == null) {
                HttpHostExt.NO_PROXY.forceInvalidate(false);

                Iterator<HttpHostExt> hostIterator = AbstractProxyListProvider.getInstance().getProxyList().iterator();
                while (hostIterator.hasNext()) {
                    HttpHostExt proxy = hostIterator.next();
                    if (proxy == null) continue;

                    BookInfo proxyBookInfo = extractBookInfo(getDocumentWithProxy(proxy));
                    if (proxyBookInfo == null) proxy.forceInvalidate(true);
                    else
                        return proxyBookInfo;
                }
            } else return defaultBookInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Document getDocumentWithProxy(HttpHostExt proxy) {
        Response resp = getContent(bookUrl, proxy, true);

        if (resp == null) return null;
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
            res = Jsoup.connect(bookUrl).userAgent(HttpConnections.USER_AGENT).followRedirects(false).method(Connection.Method.GET).execute();
        } catch (UnknownHostException uhe) {
            logger.severe("Not connected to Internet!");
        } catch (Exception ex) {
            try {
                res = Jsoup.connect(HTTP_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, bookId) + OPEN_PAGE_ADD_URL).userAgent(HttpConnections.USER_AGENT).method(Connection.Method.GET).execute();
            } catch (Exception ex1) {
                throw new RuntimeException(ex1);
            }
        }

        try {
            if (res != null) {
                doc = res.parse();
                HttpConnections.setDefaultCookies(res.cookies());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return doc;
    }

    private BookInfo extractBookInfo(Document doc) throws IOException {
        if (doc == null) return null;

        Elements scripts = doc.select("script");
        for (Element script : scripts) {
            List<Node> childs = script.childNodes();
            if (childs != null && childs.size() > 0) {
                String data = childs.get(0).attr("data");

                if (data == null || data.length() == 0) return null;

                if (data.startsWith(ADD_FLAGS_ATTRIBUTE) && data.indexOf(OC_RUN_ATTRIBUTE) > 0) {
                    int jsonStart = data.indexOf(OC_RUN_ATTRIBUTE) + OC_RUN_ATTRIBUTE.length() + 1;
                    int jsonEnd = data.lastIndexOf(BOOK_INFO_START_TAG) - 3;

                    if (jsonStart <= 0 || jsonEnd <= 0) return null;

                    String pagesJsonData = data.substring(jsonStart, jsonEnd);
                    PagesInfo pages = Mapper.objectMapper.readValue(pagesJsonData, PagesInfo.class);

                    String bookJsonData = data.substring(data.indexOf(BOOK_INFO_START_TAG) - 2, data.lastIndexOf(BOOK_INFO_END_TAG) - 3);
                    BookData bookData = Mapper.objectMapper.readValue(bookJsonData, BookData.class);

                    return new BookInfo(bookData, pages, bookId);
                }
            }
        }

        return null;
    }

    public BookInfo getBookInfo() {
        return bookInfo;
    }
}
