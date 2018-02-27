package ru.kmorozov.library.data.loader.processors;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.core.logic.Proxy.EmptyProxyListProvider;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.Proxy.ManagedProxyListProvider;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.connectors.google.GoogleHttpConnector;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.IdType;
import ru.kmorozov.library.data.repository.BooksRepository;
import ru.kmorozov.library.data.repository.RxBooksRepository;

import java.io.IOException;
import java.util.Map;

@Component
public class JstorProcessor {

    private static final String JSTOR_ARTICLE_PREFIX = "https://www.jstor.org/stable/";
    private static final String JSTOR_CITATION_PREFIX = "https://www.jstor.org/citation/text/";

    @Value("${mongo.reactive.mode}")
    private boolean reactiveMode;

    @Autowired
    private ManagedProxyListProvider proxyProvider;

    protected static final Logger logger = Logger.getLogger(JstorProcessor.class);

    private static final HttpConnector connector = new GoogleHttpConnector();

    @Autowired
    protected BooksRepository booksRepository;

    @Autowired
    protected RxBooksRepository rxBooksRepository;

    @Bean
    public ManagedProxyListProvider proxyProvider() {
        return new ManagedProxyListProvider(EmptyProxyListProvider.INSTANCE, 5000);
    }

    public void process() {
        logger.info("Process JSTOR started.");

        if (reactiveMode)
            rxBooksRepository
                    .findBooksByRegexBookInfoFileName("^\\d+.pdf")
                    .subscribe()
                    .dispose();
        else
            booksRepository
                    .findBooksByRegexBookInfoFileName("^\\d+.pdf")
                    .parallelStream()
                    .forEach(this::processJstorBook);

        logger.info("Process JSTOR finished.");
    }

    private void processJstorBook(Book jstorBook) {
        String fileName = jstorBook.getBookInfo().getFileName();
        String jstorId = fileName.substring(0, fileName.length() - 4);

        HttpHostExt proxy = proxyProvider.getProxy();

        String doi = null;
        try {
            Document doc = connector.getHtmlDocument(JSTOR_ARTICLE_PREFIX + jstorId, proxy, true);
            doi = doc.getElementsByAttributeValue("name", "ST.discriminator").attr("content");
        } catch (IOException e) {
            logger.error(String.format("%s not a valid JSTOR id", jstorId), e);
            return;
        }

        jstorBook.addBookId(IdType.JSTOR, jstorId);
        booksRepository.save(jstorBook);

        try {
            Map<String, String> doiMap = connector.getJsonMapDocument(JSTOR_CITATION_PREFIX + doi, proxy, true);
            if (!doiMap.isEmpty()) {

            }
        } catch (IOException e) {
            logger.warn(String.format("Invalid DOI %s for %s", doi, jstorId));
            return;
        }

        jstorBook.addBookId(IdType.DOI, doi);
        booksRepository.save(jstorBook);
    }
}
