package ru.kmorozov.gbd.core.logic.connectors;

import com.google.api.client.http.HttpHeaders;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.Proxy.UrlType;
import ru.kmorozov.gbd.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * Created by km on 17.05.2016.
 */
public abstract class HttpConnector {

    public static final int CONNECT_TIMEOUT = 30000;
    protected static final Logger logger = Logger.getLogger(HttpConnector.class);
    protected static final int MAX_RETRY_COUNT = 2;
    protected static final int SLEEP_TIME = 500;

    private static final Parser parser = Parser.htmlParser();

    protected static String getProxyKey(final HttpHostExt proxy) {
        return proxy.toString();
    }

    public abstract Response getContent(String url, HttpHostExt proxy, boolean withTimeout) throws IOException;

    public Document getHtmlDocument(String url, HttpHostExt proxy, boolean withTimeout) throws IOException {
        Response response = getContent(url, proxy, withTimeout);
        if (response == null)
            throw new IOException("Cannot get document!");

        return parser.parseInput(new StringReader(IOUtils.toString(response.getContent(), Charset.forName("UTF-8"))), url);
    }

    public abstract void close();

    public UrlType getUrlType(String url) {
        if (url.contains("books.google"))
            return UrlType.GOOGLE_BOOKS;
        else if (url.contains("jstor"))
            return UrlType.JSTOR;
        else
            return UrlType.OTHER;
    }

    protected boolean needHeaders(String url) {
        return getUrlType(url) != UrlType.OTHER;
    }

    protected boolean validateProxy(String url, HttpHostExt proxy) {
        if (!needHeaders(url))
            return true;

        UrlType urlType = getUrlType(url);

        HttpHeaders headers = proxy.getHeaders(urlType);

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
