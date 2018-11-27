package ru.kmorozov.gbd.core.logic.extractors.base;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.kmorozov.db.core.config.IContextLoader;
import ru.kmorozov.db.core.logic.model.book.BookInfo;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.Response;
import ru.kmorozov.gbd.core.logic.context.ContextProvider;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.gbd.utils.HttpConnections;

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

    public AbstractBookExtractor() {
    }

    protected AbstractBookExtractor(String bookId) {
        this(bookId, ContextProvider.getContextProvider());
    }

    protected AbstractBookExtractor(String bookId, final IContextLoader storedLoader) {
        this.bookId = bookId;

        BookInfo storedBookInfo = storedLoader == null ? null : storedLoader.getBookInfo(bookId);
        try {
            this.bookInfo = null == storedBookInfo ? this.findBookInfo() : storedBookInfo;
        } catch (final Exception e) {
            e.printStackTrace();

            this.bookInfo = null;
        }
    }

    protected abstract String getBookUrl();

    protected abstract String getReserveBookUrl();

    protected abstract BookInfo findBookInfo() throws Exception;

    protected Document getDocumentWithoutProxy() throws Exception {
        Connection.Response res = null;
        Document doc = null;

        try {
            res = Jsoup.connect(this.getBookUrl()).userAgent(HttpConnections.USER_AGENT).followRedirects(false).timeout(20000).method(Connection.Method.GET).execute();
        } catch (UnknownHostException uhe) {
            AbstractBookExtractor.logger.severe("Not connected to Internet!");
        } catch (Exception ex) {
            try {
                res = Jsoup.connect(this.getReserveBookUrl()).userAgent(HttpConnections.USER_AGENT).method(Connection.Method.GET).execute();
            } catch (Exception ex1) {
                throw new Exception(ex1);
            }
        }

        try {
            if (null != res) {
                doc = res.parse();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return doc;
    }

    protected Document getDocumentWithProxy(HttpHostExt proxy) {
        Response resp = AbstractHttpProcessor.getContent(this.getBookUrl(), proxy, true);

        if (null == resp) return null;
        else {
            try (final InputStream is = resp.getContent()) {
                String respStr = new String(is.readAllBytes(), Charset.defaultCharset());
                return Jsoup.parse(respStr);
            } catch (IOException e) {
                return null;
            }
        }
    }

    public BookInfo getBookInfo() {
        return this.bookInfo;
    }
}
