package ru.kmorozov.gbd.core.logic.Proxy.web;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.kmorozov.gbd.utils.HttpConnections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by km on 17.12.2016.
 */
public abstract class AbstractProxyExtractor {

    public List<String> getProxyList() {
        try {
            Document doc = Jsoup.connect(this.getProxyListUrl()).userAgent(HttpConnections.USER_AGENT).get();
            return this.extractProxyList(doc);
        } catch (IOException e) {
        }

        return new ArrayList<>();
    }

    protected abstract String getProxyListUrl();

    protected abstract List<String> extractProxyList(Document doc);
}
