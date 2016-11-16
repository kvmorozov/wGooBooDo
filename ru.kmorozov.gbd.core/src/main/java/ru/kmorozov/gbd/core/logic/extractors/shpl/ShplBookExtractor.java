package ru.kmorozov.gbd.core.logic.extractors.shpl;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookExtractor;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData;
import ru.kmorozov.gbd.core.logic.model.book.shpl.ShplBookData;
import ru.kmorozov.gbd.core.logic.model.book.shpl.ShplPage;
import ru.kmorozov.gbd.core.logic.model.book.shpl.ShplPagesInfo;
import ru.kmorozov.gbd.core.utils.Mapper;

import java.io.IOException;
import java.util.List;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public class ShplBookExtractor extends AbstractBookExtractor {

    private static final String JSON_TAG_PAGES = "pages: ";

    public ShplBookExtractor(String bookId) {
        super(bookId);
    }

    @Override
    protected String getBookUrl() {
        return bookId;
    }

    @Override
    protected String getReserveBookUrl() {
        return getBookUrl();
    }

    @Override
    public BookInfo getBookInfo() {
        return bookInfo;
    }

    @Override
    protected BookInfo findBookInfo() {
        Document defaultDocument = getDocumentWithoutProxy();
        return extractBookInfo(defaultDocument);
    }

    private BookInfo extractBookInfo(Document doc) {
        if (doc == null) return null;

        Element title = doc.select("title").get(0);
        IBookData bookData = new ShplBookData(title.textNodes().get(0).text().split("\\|")[1]);
        ShplPagesInfo pagesInfo = null;

        Elements scripts = doc.select("script");
        for (Element script : scripts) {
            List<Node> childs = script.childNodes();
            if (childs != null && childs.size() > 0) {
                String data = childs.get(0).attr("data");

                if (data == null || data.length() == 0) return null;

                if (data.contains(JSON_TAG_PAGES)) {
                    String pagesData = "[" + data.split("[|]")[2].split("\\[|\\]")[3] + "]";

                    try {
                        ShplPage[] pages = Mapper.objectMapper.readValue(pagesData, ShplPage[].class);
                        pagesInfo = new ShplPagesInfo(pages);
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return new BookInfo(bookData, pagesInfo, bookId);
    }
}
