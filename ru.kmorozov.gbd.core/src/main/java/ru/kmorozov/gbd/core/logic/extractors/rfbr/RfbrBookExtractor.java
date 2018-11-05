package ru.kmorozov.gbd.core.logic.extractors.rfbr;

import org.jsoup.nodes.Document;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookExtractor;
import ru.kmorozov.db.core.logic.model.book.base.BookInfo;
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData;
import ru.kmorozov.db.core.logic.model.book.rfbr.RfbrBookData;
import ru.kmorozov.db.core.logic.model.book.rfbr.RfbrPage;
import ru.kmorozov.db.core.logic.model.book.rfbr.RfbrPagesInfo;

import java.util.Arrays;

public class RfbrBookExtractor extends AbstractBookExtractor {

    private static final String RFBR_BASE_URL = "http://www.rfbr.ru/rffi/ru/books/o_";

    public RfbrBookExtractor(String bookId) {
        super(bookId);
    }

    @Override
    protected String getBookUrl() {
        return RFBR_BASE_URL + bookId;
    }

    @Override
    protected String getReserveBookUrl() {
        return getBookUrl();
    }

    @Override
    protected BookInfo findBookInfo() {
        Document defaultDocument = null;
        try {
            defaultDocument = getDocumentWithoutProxy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return extractBookInfo(defaultDocument);
    }

    private BookInfo extractBookInfo(Document doc) {
        if (null == doc) return null;

        final IBookData bookData = new RfbrBookData(bookId);
        int numPages = Integer.valueOf(Arrays.stream(doc.html().split("\\r?\\n")).filter(s -> s.contains("readerInitialization")).findAny().get().split("\\(")[1].split(",")[0]);

        RfbrPage[] pages = new RfbrPage[numPages];

        for (int index = 0; index < numPages; index++)
            pages[index] = new RfbrPage(bookId, index);

        return new BookInfo(bookData, new RfbrPagesInfo(pages), bookId);
    }
}
