package ru.kmorozov.library.data.loader.processors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.gbd.utils.HttpConnections;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.IdInfo;
import ru.kmorozov.library.data.repository.BooksRepository;

import java.io.IOException;
import java.util.stream.Stream;

@Component
public class JstorProcessor {

    private static final String JSTOR_ARTICLE_PREFIX = "https://www.jstor.org/stable/";
    private static final String JSTOR_CITATION_PREFIX = "https://www.jstor.org/citation/text/";

    protected static final Logger logger = Logger.getLogger(JstorProcessor.class);

    @Autowired
    protected BooksRepository booksRepository;

    public void process() {
        logger.info("Process JSTOR started.");

        Stream<Book> stream = booksRepository.findBooksByRegexBookInfoFileName("^\\d+.pdf").parallelStream();
        stream.forEach(jstorBook -> {
            String fileName = jstorBook.getBookInfo().getFileName();
            String jstorId = fileName.substring(0, fileName.length() - 4);

            try {
                Document doc = Jsoup.connect(JSTOR_ARTICLE_PREFIX + jstorId).userAgent(HttpConnections.USER_AGENT).get();
                Elements doi = doc.getElementsByAttributeValue("name", "item_doi");

                jstorBook.addBookId(new IdInfo(jstorId, IdInfo.IdType.JSTOR));
            } catch (IOException e) {
                logger.warn(String.format("%s not a valid JSTOR id", jstorId));
            }
        });

        logger.info("Process JSTOR finished.");
    }
}
