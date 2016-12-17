package ru.kmorozov.gbd.core.logic.Proxy.web;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.kmorozov.gbd.core.utils.HttpConnections;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by km on 17.12.2016.
 */
public abstract class AbstractProxyExtractor {

    public List<String> getProxyList() {
        try {
            Document doc = Jsoup.connect(getProxyListUrl()).userAgent(HttpConnections.USER_AGENT).get();
            return extractProxyList(doc);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.EMPTY_LIST;
    }

    protected abstract String getProxyListUrl();

    protected abstract List<String> extractProxyList(Document doc);
}
