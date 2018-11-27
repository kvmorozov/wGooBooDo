package ru.kmorozov.gbd.core.logic.extractors.google;

import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import ru.kmorozov.db.core.config.IContextLoader;
import ru.kmorozov.gbd.core.logic.Proxy.AbstractProxyListProvider;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookExtractor;
import ru.kmorozov.db.core.logic.model.book.BookInfo;
import ru.kmorozov.db.core.logic.model.book.google.GoogleBookData;
import ru.kmorozov.db.core.logic.model.book.google.GooglePagesInfo;
import ru.kmorozov.db.utils.Mapper;

import java.util.List;

import static ru.kmorozov.gbd.core.config.constants.GoogleConstants.*;

/**
 * Created by km on 08.10.2016.
 */
public class GoogleBookInfoExtractor extends AbstractBookExtractor {

    private static final String ADD_FLAGS_ATTRIBUTE = "_OC_addFlags";
    private static final String OC_RUN_ATTRIBUTE = "_OC_Run";
    private static final String BOOK_INFO_START_TAG = "fullview";
    private static final String BOOK_INFO_END_TAG = "enableUserFeedbackUI";
    private static final String OPEN_PAGE_ADD_URL = "&printsec=frontcover&hl=ru#v=onepage&q&f=false";

    public GoogleBookInfoExtractor(String bookId) {
        super(bookId);
    }

    public GoogleBookInfoExtractor(String bookId, final IContextLoader storedLoader) {
        super(bookId, storedLoader);
    }

    @Override
    protected String getBookUrl() {
        return HTTPS_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, this.bookId) + GoogleBookInfoExtractor.OPEN_PAGE_ADD_URL;
    }

    @Override
    protected String getReserveBookUrl() {
        return HTTP_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, this.bookId) + GoogleBookInfoExtractor.OPEN_PAGE_ADD_URL;
    }

    @Override
    protected BookInfo findBookInfo() throws Exception {
        Document defaultDocument = this.getDocumentWithoutProxy();
        try {
            BookInfo defaultBookInfo = this.extractBookInfo(defaultDocument);
            if (null == defaultBookInfo) {
                HttpHostExt.NO_PROXY.forceInvalidate(false);

                for (HttpHostExt proxy : AbstractProxyListProvider.getInstance().getProxyList()) {
                    if (null == proxy) continue;

                    BookInfo proxyBookInfo = this.extractBookInfo(this.getDocumentWithProxy(proxy));
                    if (null == proxyBookInfo) proxy.forceInvalidate(true);
                    else return proxyBookInfo;
                }
            } else return defaultBookInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private BookInfo extractBookInfo(Document doc) {
        if (null == doc) return null;

        Elements scripts = doc.select("script");
        for (Element script : scripts) {
            List<Node> childs = script.childNodes();
            if (null != childs && !childs.isEmpty() && childs.get(0) instanceof DataNode) {
                String data = ((DataNode) childs.get(0)).getWholeData();

                if (null == data || data.isEmpty()) continue;

                if (data.startsWith(GoogleBookInfoExtractor.ADD_FLAGS_ATTRIBUTE) && 0 < data.indexOf(GoogleBookInfoExtractor.OC_RUN_ATTRIBUTE)) {
                    int jsonStart = data.indexOf(GoogleBookInfoExtractor.OC_RUN_ATTRIBUTE) + GoogleBookInfoExtractor.OC_RUN_ATTRIBUTE.length() + 1;
                    int jsonEnd = data.lastIndexOf(GoogleBookInfoExtractor.BOOK_INFO_START_TAG) - 3;

                    if (0 >= jsonStart || 0 >= jsonEnd) return null;

                    String pagesJsonData = data.substring(jsonStart, jsonEnd);
                    GooglePagesInfo pages = Mapper.getGson().fromJson(pagesJsonData, GooglePagesInfo.class);

                    String bookJsonData = data.substring(data.indexOf(GoogleBookInfoExtractor.BOOK_INFO_START_TAG) - 2, data.lastIndexOf(GoogleBookInfoExtractor.BOOK_INFO_END_TAG) - 3);
                    GoogleBookData bookData = Mapper.getGson().fromJson(bookJsonData, GoogleBookData.class);

                    return new BookInfo(bookData, pages, this.bookId);
                }
            }
        }

        return null;
    }
}
