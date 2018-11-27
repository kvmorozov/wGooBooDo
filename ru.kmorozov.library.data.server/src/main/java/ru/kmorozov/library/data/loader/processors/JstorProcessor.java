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
import ru.kmorozov.library.data.model.book.BookInfo.BookType;
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
        JstorProcessor.logger.info("Process JSTOR started.");

        this.booksRepository
                .findBooksByRegexBookInfoFileName("^\\d+.pdf")
                .stream()
                .filter(book -> book.getBookInfo().getCustomFields() == null)
                .forEach(this::processJstorBook);

        JstorProcessor.logger.info("Process JSTOR finished.");
    }

    private void processJstorBook(final Book jstorBook) {
        final String fileName = jstorBook.getBookInfo().getFileName();
        final String jstorId = fileName.substring(0, fileName.length() - 4);

        final HttpHostExt proxy = this.proxyProvider.getProxy();

        final String doi;
        try {
            final String doc = this.jstorConnector.getString(JstorProcessor.JSTOR_ARTICLE_PREFIX + jstorId, proxy, true);
            final Optional<String> opDoi = Arrays.stream(doc.split("\\r?\\n"))
                    .filter(s -> s.contains("ST.discriminator"))
                    .findFirst();
            if (opDoi.isPresent())
                doi = Arrays.stream(opDoi.get().split(" "))
                        .filter(s -> s.contains("content"))
                        .map(s -> s.split("=")[1])
                        .findFirst().get().replace("\"", "");
            else {
                JstorProcessor.logger.error(String.format("%s not a valid JSTOR id", jstorId));
                return;
            }
        } catch (final IOException e) {
            JstorProcessor.logger.error(String.format("%s not a valid JSTOR id", jstorId), e);
            return;
        }

        jstorBook.addBookId(IdType.JSTOR, jstorId);
        this.booksRepository.save(jstorBook);

        try {
            final Map<String, String> doiMap = this.parseArticleData(JstorProcessor.doiConnector.getString(JstorProcessor.JSTOR_CITATION_PREFIX + doi, proxy, true));
            if (!doiMap.isEmpty()) {
                jstorBook.getBookInfo().setBookType(BookType.ARTICLE);
                jstorBook.getBookInfo().setCustomFields(doiMap);
                this.booksRepository.save(jstorBook);

                JstorProcessor.logger.info(String.format("Saved DOI data for %s", doi));
            } else
                JstorProcessor.logger.info(String.format("Not saved DOI data for %s", doi));
        } catch (final IOException e) {
            JstorProcessor.logger.warn(String.format("Invalid DOI %s for %s", doi, jstorId));
            return;
        }

        jstorBook.addBookId(IdType.DOI, doi);
        this.booksRepository.save(jstorBook);
    }

    private Map<String, String> parseArticleData(final String data) {
        return Arrays.stream(data.split("\\r?\\n"))
                .filter(s -> s.contains("="))
                .map(s -> {
                    String[] items = s.split("=");
                    return items.length == 2 ? new ImmutablePair<>(this.formatItem(items[0]), this.formatItem(items[1])) : null;
                })
                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));
    }

    private String formatItem(final String item) {
        return item.trim()
                .replace("{", "")
                .replace("},", "")
                .replace("}", "")
                .trim();
    }
}
