package ru.kmorozov.gbd.core.logic.connectors;

import com.google.api.client.http.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import ru.kmorozov.db.utils.Mapper;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.Proxy.UrlType;
import ru.kmorozov.gbd.logger.Logger;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by km on 17.05.2016.
 */
public abstract class HttpConnector implements AutoCloseable {

    public static final int CONNECT_TIMEOUT = 30000;
    protected static final Logger logger = Logger.getLogger(HttpConnector.class);
    protected static final int MAX_RETRY_COUNT = 2;
    protected static final int SLEEP_TIME = 500;

    private static final Parser parser = Parser.htmlParser();

    protected static String getProxyKey(HttpHostExt proxy) {
        return proxy.toString();
    }

    public abstract Response getContent(String url, HttpHostExt proxy, boolean withTimeout) throws IOException;

    public Document getHtmlDocument(final String url, final HttpHostExt proxy, final boolean withTimeout) throws IOException {
        try {
            final Response response = this.getContent(url, proxy, withTimeout);
            if (response == null)
                throw new IOException("Cannot get document!");

            return HttpConnector.parser.parseInput(new StringReader(new String(response.getContent().readAllBytes(), Charset.forName("UTF-8"))), url);
        } finally {
            proxy.updateTimestamp();
        }
    }

    public Map<String, String> getJsonMapDocument(final String url, final HttpHostExt proxy, final boolean withTimeout) throws IOException {
        try {
            final Response response = this.getContent(url, proxy, withTimeout);
            if (response == null)
                throw new IOException("Cannot get document!");

            return Mapper.getGson().fromJson(new String(response.getContent().readAllBytes(), Charset.forName("UTF-8")), Mapper.mapType);
        } finally {
            proxy.updateTimestamp();
        }
    }

    public String getString(final String url, final HttpHostExt proxy, final boolean withTimeout) throws IOException {
        try {
            final Response response = this.getContent(url, proxy, withTimeout);
            if (response == null)
                throw new IOException("Cannot get document!");

            return new String(response.getContent().readAllBytes(), Charset.forName("UTF-8"));
        } finally {
            proxy.updateTimestamp();
        }
    }

    public UrlType getUrlType(final String url) {
        if (url.contains("books.google"))
            return UrlType.GOOGLE_BOOKS;
        else if (url.contains("jstor"))
            return UrlType.JSTOR;
        else
            return UrlType.OTHER;
    }

    protected boolean needHeaders(final String url) {
        return this.getUrlType(url) != UrlType.OTHER;
    }

    protected boolean validateProxy(final String url, final HttpHostExt proxy) {
        if (!this.needHeaders(url))
            return true;

        final UrlType urlType = this.getUrlType(url);

        final HttpHeaders headers = proxy.getHeaders(urlType);

        if (StringUtils.isEmpty(headers.getCookie()))
            return false;

        switch (urlType) {
            case GOOGLE_BOOKS:
                return headers.getCookie().contains("NID");
            case JSTOR:
                return headers.getCookie().contains("UUID");
            default:
                return true;
        }
    }
}
