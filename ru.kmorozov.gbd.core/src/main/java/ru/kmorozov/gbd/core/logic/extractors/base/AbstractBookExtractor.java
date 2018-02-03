package ru.kmorozov.gbd.core.logic.extractors.base;

import org.apache.commons.io.IOUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.kmorozov.gbd.core.config.storage.AbstractContextProvider;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.Response;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;
import ru.kmorozov.gbd.utils.HttpConnections;
import ru.kmorozov.gbd.utils.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public abstract class AbstractBookExtractor extends AbstractHttpProcessor {

    protected static final Logger logger = ExecutionContext.INSTANCE.getLogger(AbstractBookExtractor.class);

    protected String bookId;
    protected BookInfo bookInfo;

    protected AbstractBookExtractor(final String bookId) {
        this.bookId = bookId;

        final BookInfo storedBookInfo = AbstractContextProvider.getContextProvider().getBookInfo(bookId);
        bookInfo = null == storedBookInfo ? findBookInfo() : storedBookInfo;
    }

    protected abstract String getBookUrl();

    protected abstract String getReserveBookUrl();

    protected abstract BookInfo findBookInfo();

    protected Document getDocumentWithoutProxy() {
        Connection.Response res = null;
        Document doc = null;

        try {
            res = Jsoup.connect(getBookUrl()).userAgent(HttpConnections.USER_AGENT).followRedirects(false).timeout(20000).method(Method.GET).execute();
        } catch (final UnknownHostException uhe) {
            logger.severe("Not connected to Internet!");
        } catch (final Exception ex) {
            try {
                res = Jsoup.connect(getReserveBookUrl()).userAgent(HttpConnections.USER_AGENT).method(Method.GET).execute();
            } catch (final Exception ex1) {
                throw new RuntimeException(ex1);
            }
        }

        try {
            if (null != res) {
                doc = res.parse();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return doc;
    }

    protected Document getDocumentWithProxy(final HttpHostExt proxy) {
        final Response resp = getContent(getBookUrl(), proxy, true);

        if (null == resp) return null;
        else {
            try (InputStream is = resp.getContent()) {
                final String respStr = IOUtils.toString(is, Charset.defaultCharset());
                return Jsoup.parse(respStr);
            } catch (final IOException e) {
                return null;
            }
        }
    }

    public abstract BookInfo getBookInfo();
}
