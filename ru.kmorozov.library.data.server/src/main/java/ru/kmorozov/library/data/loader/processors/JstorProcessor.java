package ru.kmorozov.library.data.loader.processors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.core.logic.Proxy.EmptyProxyListProvider;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.Proxy.ManagedProxyListProvider;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.connectors.google.GoogleHttpConnector;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.BookInfo;
import ru.kmorozov.library.data.model.book.IdType;
import ru.kmorozov.library.data.repository.BooksRepository;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JstorProcessor implements IProcessor {

    private static final String JSTOR_ARTICLE_PREFIX = "https://www.jstor.org/stable/";
    private static final String JSTOR_CITATION_PREFIX = "https://www.jstor.org/citation/text/";

    @Value("${mongo.reactive.mode}")
    private boolean reactiveMode;

    @Autowired
    private ManagedProxyListProvider proxyProvider;

    protected static final Logger logger = Logger.getLogger(JstorProcessor.class);

    @Autowired @Lazy
    private HttpConnector jstorConnector;

    private static final HttpConnector doiConnector = new GoogleHttpConnector();

    @Autowired
    protected BooksRepository booksRepository;

    @Bean
    public ManagedProxyListProvider proxyProvider() {
        return new ManagedProxyListProvider(EmptyProxyListProvider.INSTANCE, 500);
    }

    @Override
    public void process() {
        logger.info("Process JSTOR started.");

        booksRepository
                .findBooksByRegexBookInfoFileName("^\\d+.pdf")
                .stream()
                .filter(book -> book.getBookInfo().getCustomFields() == null)
                .forEach(this::processJstorBook);

        logger.info("Process JSTOR finished.");
    }

    private void processJstorBook(Book jstorBook) {
        String fileName = jstorBook.getBookInfo().getFileName();
        String jstorId = fileName.substring(0, fileName.length() - 4);

        HttpHostExt proxy = proxyProvider.getProxy();

        String doi;
        try {
            String doc = jstorConnector.getString(JSTOR_ARTICLE_PREFIX + jstorId, proxy, true);
            Optional<String> opDoi = Arrays.stream(doc.split("\\r?\\n"))
                    .filter(s -> s.contains("ST.discriminator"))
                    .findFirst();
            if (opDoi.isPresent())
                doi = Arrays.stream(opDoi.get().split(" "))
                        .filter(s -> s.contains("content"))
                        .map(s -> s.split("=")[1])
                        .findFirst().get().replace("\"", "");
            else {
                logger.error(String.format("%s not a valid JSTOR id", jstorId));
                return;
            }
        } catch (IOException e) {
            logger.error(String.format("%s not a valid JSTOR id", jstorId), e);
            return;
        }

        jstorBook.addBookId(IdType.JSTOR, jstorId);
        booksRepository.save(jstorBook);

        try {
            Map<String, String> doiMap = parseArticleData(doiConnector.getString(JSTOR_CITATION_PREFIX + doi, proxy, true));
            if (!doiMap.isEmpty()) {
                jstorBook.getBookInfo().setBookType(BookInfo.BookType.ARTICLE);
                jstorBook.getBookInfo().setCustomFields(doiMap);
                booksRepository.save(jstorBook);

                logger.info(String.format("Saved DOI data for %s", doi));
            } else
                logger.info(String.format("Not saved DOI data for %s", doi));
        } catch (IOException e) {
            logger.warn(String.format("Invalid DOI %s for %s", doi, jstorId));
            return;
        }

        jstorBook.addBookId(IdType.DOI, doi);
        booksRepository.save(jstorBook);
    }

    private Map<String, String> parseArticleData(String data) {
        return Arrays.stream(data.split("\\r?\\n"))
                .filter(s -> s.contains("="))
                .map(s -> {
                    String[] items = s.split("=");
                    return items.length == 2 ? new ImmutablePair<>(formatItem(items[0]), formatItem(items[1])) : null;
                })
                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));
    }

    private String formatItem(String item) {
        return item.trim()
                .replace("{", "")
                .replace("},", "")
                .replace("}", "")
                .trim();
    }
}
