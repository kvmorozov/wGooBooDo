package ru.kmorozov.gbd.core.logic.extractors.google;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import ru.kmorozov.gbd.core.logic.Proxy.AbstractProxyListProvider;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookExtractor;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;
import ru.kmorozov.gbd.core.logic.model.book.google.BookData;
import ru.kmorozov.gbd.core.logic.model.book.google.GogglePagesInfo;
import ru.kmorozov.gbd.core.utils.Logger;
import ru.kmorozov.gbd.core.utils.Mapper;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static ru.kmorozov.gbd.core.logic.context.ExecutionContext.INSTANCE;
import static ru.kmorozov.gbd.core.logic.extractors.google.GoogleImageExtractor.*;

/**
 * Created by km on 08.10.2016.
 */
public class GoogleBookInfoExtractor extends AbstractBookExtractor {

    private static final Logger logger = INSTANCE.getLogger(GoogleImageExtractor.class);

    private static final String ADD_FLAGS_ATTRIBUTE = "_OC_addFlags";
    private static final String OC_RUN_ATTRIBUTE = "_OC_Run";
    private static final String BOOK_INFO_START_TAG = "fullview";
    private static final String BOOK_INFO_END_TAG = "enableUserFeedbackUI";
    private static final String OPEN_PAGE_ADD_URL = "&printsec=frontcover&hl=ru#v=onepage&q&f=false";

    public GoogleBookInfoExtractor(String bookId) {
        super(bookId);
    }

    @Override
    protected String getBookUrl() {
        return HTTPS_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, bookId) + OPEN_PAGE_ADD_URL;
    }

    @Override
    protected String getReserveBookUrl() {
        return HTTP_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, bookId) + OPEN_PAGE_ADD_URL;
    }

    @Override
    protected BookInfo findBookInfo() {
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
                    GogglePagesInfo pages = Mapper.objectMapper.readValue(pagesJsonData, GogglePagesInfo.class);

                    String bookJsonData = data.substring(data.indexOf(BOOK_INFO_START_TAG) - 2, data.lastIndexOf(BOOK_INFO_END_TAG) - 3);
                    BookData bookData = Mapper.objectMapper.readValue(bookJsonData, BookData.class);

                    return new BookInfo(bookData, pages, bookId);
                }
            }
        }

        return null;
    }

    @Override
    public BookInfo getBookInfo() {
        return (BookInfo) bookInfo;
    }
}
