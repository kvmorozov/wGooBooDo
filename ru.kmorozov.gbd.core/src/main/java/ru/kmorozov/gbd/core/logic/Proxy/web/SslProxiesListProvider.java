package ru.kmorozov.gbd.core.logic.Proxy.web;

import com.google.api.client.util.Strings;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by km on 17.12.2016.
 */
public class SslProxiesListProvider extends AbstractProxyExtractor {

    private static final String PROXY_LIST_URL = "http://www.proxz.com/proxy_list_anonymous_us_0.html";
    private static final String ipPortPattern = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]):[0-9]+";
    private static final Pattern pattern = Pattern.compile(ipPortPattern, Pattern.LITERAL);
    private static boolean checkRegexp = false;

    @Override
    protected String getProxyListUrl() {
        return PROXY_LIST_URL;
    }

    @Override
    protected List<String> extractProxyList(final Document doc) {
        String textWithProxies = doc.html().replaceAll("<", "|").replaceAll(">", "|");
        return Arrays.stream(textWithProxies.split("\\|")).filter(s -> validIpPort(s)).collect(Collectors.toList());
    }

    private boolean validIpPort(String str) {
        if (Strings.isNullOrEmpty(str))
            return false;

        if (str.length() < 10 || str.length() > 20)
            return false;

        if (StringUtils.countMatches(str, ".") != 3 || !str.contains(":"))
            return false;

        return checkRegexp ? pattern.matcher(str).matches() : true;
    }
}
