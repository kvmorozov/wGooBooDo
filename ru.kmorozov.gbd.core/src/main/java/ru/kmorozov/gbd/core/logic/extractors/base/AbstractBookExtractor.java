package ru.kmorozov.gbd.core.logic.extractors.base;

import org.apache.commons.io.IOUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.kmorozov.gbd.core.config.storage.AbstractContextProvider;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.Response;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;
import ru.kmorozov.gbd.core.utils.HttpConnections;
import ru.kmorozov.gbd.core.utils.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import static ru.kmorozov.gbd.core.logic.context.ExecutionContext.INSTANCE;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public abstract class AbstractBookExtractor extends AbstractHttpProcessor {

    protected static final Logger logger = INSTANCE.getLogger(AbstractBookExtractor.class);

    protected String bookId;
    protected BookInfo bookInfo;

    protected AbstractBookExtractor(String bookId) {
        this.bookId = bookId;

        BookInfo storedBookInfo = AbstractContextProvider.getContextProvider().getBookInfo(bookId);
        bookInfo = storedBookInfo == null ? findBookInfo() : storedBookInfo;
    }

    protected abstract String getBookUrl();

    protected abstract String getReserveBookUrl();

    protected abstract BookInfo findBookInfo();

    protected Document getDocumentWithoutProxy() {
        Connection.Response res = null;
        Document doc = null;

        try {
            res = Jsoup.connect(getBookUrl()).userAgent(HttpConnections.USER_AGENT).followRedirects(false).timeout(20000).method(Connection.Method.GET).execute();
        } catch (UnknownHostException uhe) {
            logger.severe("Not connected to Internet!");
        } catch (Exception ex) {
            try {
                res = Jsoup.connect(getReserveBookUrl()).userAgent(HttpConnections.USER_AGENT).method(Connection.Method.GET).execute();
            } catch (Exception ex1) {
                throw new RuntimeException(ex1);
            }
        }

        return doc;
    }

    protected Document getDocumentWithProxy(HttpHostExt proxy) {
        Response resp = getContent(getBookUrl(), proxy, true);

        if (resp == null) return null;
        else {
            try (InputStream is = resp.getContent()) {
                String respStr = IOUtils.toString(is, Charset.defaultCharset());
                return Jsoup.parse(respStr);
            } catch (IOException e) {
                return null;
            }
        }
    }

    public abstract BookInfo getBookInfo();
}
