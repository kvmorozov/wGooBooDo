package ru.kmorozov.gbd.core.logic.Proxy.web;

import ru.kmorozov.gbd.core.logic.Proxy.AbstractProxyListProvider;
import ru.kmorozov.gbd.core.logic.Proxy.ProxyBlacklistHolder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by km on 23.11.2015.
 */
public class WebProxyListProvider extends AbstractProxyListProvider {

    public WebProxyListProvider() {
        buildList();
    }

    private void buildList() {
        List<String> candidateProxies = (new SslProxiesListProvider()).getProxyList();
        candidateProxies.addAll((new HideMeListProvider()).getProxyList());

        this.proxyItems = candidateProxies.stream().filter(this::notBlacklisted).limit(20).collect(Collectors.toSet());
        this.proxyItems.addAll(ProxyBlacklistHolder.BLACKLIST.getWhiteList());
    }

    @Override
    public void updateProxyList() {

    }
}
