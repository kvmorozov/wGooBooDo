package ru.kmorozov.gbd.core.logic.extractors.shpl;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookExtractor;
import ru.kmorozov.db.core.logic.model.book.BookInfo;
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData;
import ru.kmorozov.db.core.logic.model.book.shpl.ShplBookData;
import ru.kmorozov.db.core.logic.model.book.shpl.ShplPage;
import ru.kmorozov.db.core.logic.model.book.shpl.ShplPagesInfo;
import ru.kmorozov.db.utils.Mapper;

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
        return this.bookId;
    }

    @Override
    protected String getReserveBookUrl() {
        return this.bookId;
    }

    @Override
    protected BookInfo findBookInfo() {
        Document defaultDocument = null;
        try {
            defaultDocument = this.getDocumentWithoutProxy();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return this.extractBookInfo(defaultDocument);
    }

    private BookInfo extractBookInfo(Document doc) {
        if (null == doc) return null;

        Element title = doc.select("title").get(0);
        IBookData bookData = new ShplBookData(title.textNodes().get(0).text().split("\\|")[1]);
        ShplPagesInfo pagesInfo = null;

        Elements scripts = doc.select("script");
        for (Element script : scripts) {
            List<Node> childs = script.childNodes();
            if (null != childs && !childs.isEmpty()) {
                String data = childs.get(0).toString();

                if (null == data || data.isEmpty()) continue;

                if (data.contains(ShplBookExtractor.JSON_TAG_PAGES)) {
                    String pagesData = '[' + data.split("[|]")[2].split("\\[|\\]")[3] + ']';

                    ShplPage[] pages = Mapper.getGson().fromJson(pagesData, ShplPage[].class);
                    for (int i = 1; i <= pages.length; i++)
                        pages[i - 1].setOrder(i);
                    pagesInfo = new ShplPagesInfo(pages);
                    break;
                }
            }
        }

        return new BookInfo(bookData, pagesInfo, this.bookId);
    }
}
