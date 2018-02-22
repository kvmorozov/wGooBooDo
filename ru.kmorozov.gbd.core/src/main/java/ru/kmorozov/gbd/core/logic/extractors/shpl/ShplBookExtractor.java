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
import ru.kmorozov.gbd.utils.Mapper;

import java.util.List;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public class ShplBookExtractor extends AbstractBookExtractor {

    private static final String JSON_TAG_PAGES = "pages: ";

    public ShplBookExtractor(final String bookId) {
        super(bookId);
    }

    @Override
    protected String getBookUrl() {
        return bookId;
    }

    @Override
    protected String getReserveBookUrl() {
        return bookId;
    }

    @Override
    protected BookInfo findBookInfo() {
        final Document defaultDocument = getDocumentWithoutProxy();
        return extractBookInfo(defaultDocument);
    }

    private BookInfo extractBookInfo(final Document doc) {
        if (null == doc) return null;

        final Element title = doc.select("title").get(0);
        final IBookData bookData = new ShplBookData(title.textNodes().get(0).text().split("\\|")[1]);
        ShplPagesInfo pagesInfo = null;

        final Elements scripts = doc.select("script");
        for (final Element script : scripts) {
            final List<Node> childs = script.childNodes();
            if (null != childs && !childs.isEmpty()) {
                final String data = childs.get(0).toString();

                if (null == data || data.isEmpty()) continue;

                if (data.contains(JSON_TAG_PAGES)) {
                    final String pagesData = '[' + data.split("[|]")[2].split("\\[|\\]")[3] + ']';

                    final ShplPage[] pages = Mapper.getGson().fromJson(pagesData, ShplPage[].class);
                    for (int i = 1; i <= pages.length; i++)
                        pages[i - 1].setOrder(i);
                    pagesInfo = new ShplPagesInfo(pages);
                    break;
                }
            }
        }

        return new BookInfo(bookData, pagesInfo, bookId);
    }
}
