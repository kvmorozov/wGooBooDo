package ru.simpleGBD.App.Logic.Proxy;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.apache.http.HttpHost;
import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Utils.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by km on 27.11.2015.
 */
public abstract class AbstractProxyListProvider implements IProxyListProvider {

    private static Logger logger = Logger.getLogger(ExecutionContext.output, "ProxyPistProvider");

    private static IProxyListProvider INSTANCE;

    protected List<HttpHostExt> proxyList = new ArrayList<>();

    protected void buildFromList(List<String> proxyItems) {
        for (String proxyItem : proxyItems) {
            String[] proxyItemArr = proxyItem.split(":");

            if (proxyItemArr == null || proxyItemArr.length != 2)
                continue;

            HttpHost host = new HttpHost(proxyItemArr[0], Integer.parseInt(proxyItemArr[1]));
            String cookie = getCookie(host);
            if (cookie != null) {
                proxyList.add(new HttpHostExt(host, cookie));
                logger.info(String.format("Proxy %s added.", host.toHostString()));
            } else
                logger.info(String.format("Proxy %s NOT added.", host.toHostString()));
        }
    }

    private String getCookie(HttpHost proxy) {
        try {
            HttpResponse resp = new NetHttpTransport.Builder().
                    setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.getHostName(), proxy.getPort()))).
                    build().createRequestFactory().buildGetRequest(new GenericUrl(ExecutionContext.baseUrl)).execute();

            return ((ArrayList) resp.getHeaders().get("set-cookie")).get(0).toString();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public List<HttpHostExt> getProxyList() {
        return proxyList;
    }

    public static IProxyListProvider getInstance() {
        if (INSTANCE == null)
            INSTANCE = GBDOptions.getProxyListFile() == null ? new StaticProxyListProvider() : new FileProxyListProvider();

        return INSTANCE;
    }
}
