package ru.kmorozov.gbd.core.logic.Proxy.web;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by km on 17.12.2016.
 */
public class HideMeListProvider extends AbstractProxyExtractor {

    private static final String PROXY_LIST_URL = "http://hideme.ru/proxy-list/?type=s#list";

    @Override
    protected String getProxyListUrl() {
        return HideMeListProvider.PROXY_LIST_URL;
    }

    @Override
    protected List<String> extractProxyList(Document doc) {
        return doc.getElementsByClass("proxy-table").get(0).
                getElementsByTag("tbody").get(0).getElementsByTag("tr").stream().map(HideMeListProvider::extractProxyData).collect(Collectors.toList());
    }

    private static String extractProxyData(Element element) {
        return ((TextNode) element.childNode(0).childNode(0)).getWholeText() + ':' + ((TextNode) element.childNode(1).childNode(0)).getWholeText();
    }
}
