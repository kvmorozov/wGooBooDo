package ru.kmorozov.gbd.core.logic.extractors.google;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import ru.kmorozov.gbd.core.logic.Proxy.AbstractProxyListProvider;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookExtractor;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;
import ru.kmorozov.gbd.core.logic.model.book.google.GoogleBookData;
import ru.kmorozov.gbd.core.logic.model.book.google.GooglePagesInfo;
import ru.kmorozov.gbd.core.utils.Logger;
import ru.kmorozov.gbd.core.utils.gson.Mapper;

import java.util.List;

/**
 * Created by km on 08.10.2016.
 */
public class GoogleBookInfoExtractor extends AbstractBookExtractor {

    private static final Logger logger = ExecutionContext.INSTANCE.getLogger(GoogleImageExtractor.class);

    private static final String ADD_FLAGS_ATTRIBUTE = "_OC_addFlags";
    private static final String OC_RUN_ATTRIBUTE = "_OC_Run";
    private static final String BOOK_INFO_START_TAG = "fullview";
    private static final String BOOK_INFO_END_TAG = "enableUserFeedbackUI";
    private static final String OPEN_PAGE_ADD_URL = "&printsec=frontcover&hl=ru#v=onepage&q&f=false";

    public GoogleBookInfoExtractor(final String bookId) {
        super(bookId);
    }

    @Override
    protected String getBookUrl() {
        return GoogleImageExtractor.HTTPS_TEMPLATE.replace(GoogleImageExtractor.BOOK_ID_PLACEHOLDER, bookId) + OPEN_PAGE_ADD_URL;
    }

    @Override
    protected String getReserveBookUrl() {
        return GoogleImageExtractor.HTTP_TEMPLATE.replace(GoogleImageExtractor.BOOK_ID_PLACEHOLDER, bookId) + OPEN_PAGE_ADD_URL;
    }

    @Override
    protected BookInfo findBookInfo() {
        final Document defaultDocument = getDocumentWithoutProxy();
        try {
            final BookInfo defaultBookInfo = extractBookInfo(defaultDocument);
            if (null == defaultBookInfo) {
                HttpHostExt.NO_PROXY.forceInvalidate(false);

                for (final HttpHostExt proxy : AbstractProxyListProvider.getInstance().getProxyList()) {
                    if (null == proxy) continue;

                    final BookInfo proxyBookInfo = extractBookInfo(getDocumentWithProxy(proxy));
                    if (null == proxyBookInfo) proxy.forceInvalidate(true);
                    else return proxyBookInfo;
                }
            }
            else return defaultBookInfo;
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private BookInfo extractBookInfo(final Document doc) {
        if (null == doc) return null;

        final Elements scripts = doc.select("script");
        for (final Element script : scripts) {
            final List<Node> childs = script.childNodes();
            if (null != childs && !childs.isEmpty()) {
                final String data = childs.get(0).attr("data");

                if (null == data || data.isEmpty()) return null;

                if (data.startsWith(ADD_FLAGS_ATTRIBUTE) && 0 < data.indexOf(OC_RUN_ATTRIBUTE)) {
                    final int jsonStart = data.indexOf(OC_RUN_ATTRIBUTE) + OC_RUN_ATTRIBUTE.length() + 1;
                    final int jsonEnd = data.lastIndexOf(BOOK_INFO_START_TAG) - 3;

                    if (0 >= jsonStart || 0 >= jsonEnd) return null;

                    final String pagesJsonData = data.substring(jsonStart, jsonEnd);
                    final GooglePagesInfo pages = Mapper.getGson().fromJson(pagesJsonData, GooglePagesInfo.class);

                    final String bookJsonData = data.substring(data.indexOf(BOOK_INFO_START_TAG) - 2, data.lastIndexOf(BOOK_INFO_END_TAG) - 3);
                    final GoogleBookData bookData = Mapper.getGson().fromJson(bookJsonData, GoogleBookData.class);

                    return new BookInfo(bookData, pages, bookId);
                }
            }
        }

        return null;
    }

    @Override
    public BookInfo getBookInfo() {
        return bookInfo;
    }
}
