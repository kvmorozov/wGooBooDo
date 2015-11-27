package ru.simpleGBD.App.Utils;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * Created by km on 22.11.2015.
 */
public class HttpConnections {

    public static HttpConnections INSTANCE = new HttpConnections();

    private BasicCookieStore cookieStore = new BasicCookieStore();
    private HttpClientBuilder builder;

    private HttpConnections() {

    }

    public BasicCookieStore getCookieStore() {
        return cookieStore;
    }

    public void setCookies(Map<String, String> cookiesMap) {
        for (Map.Entry<String, String> cookieEntry : cookiesMap.entrySet()) {
            BasicClientCookie cookie = new BasicClientCookie(cookieEntry.getKey(), cookieEntry.getValue());
            cookie.setDomain(".google.ru");
            cookie.setPath("/");
            cookieStore.addCookie(cookie);
        }
    }

    public HttpClientBuilder getBuilder() {
        if (builder != null)
            return builder;

        builder = HttpClientBuilder.create();

        try {
            // setup a Trust Strategy that allows all certificates.
            //
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    return true;
                }
            }).build();
            builder.setSSLContext(sslContext);

            // don't check Hostnames, either.
            //      -- use SSLConnectionSocketFactory.getDefaultHostnameVerifier(), if you don't want to weaken
            HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;

            // here's the special part:
            //      -- need to create an SSL Socket Factory, to use our weakened "trust strategy";
            //      -- and create a Registry, to register it.
            //
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslSocketFactory)
                    .build();

            // now, we create connection-manager using our Registry.
            //      -- allows multi-threaded use
//            PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
//            builder.setConnectionManager(connMgr);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }

        return builder;
    }
}
