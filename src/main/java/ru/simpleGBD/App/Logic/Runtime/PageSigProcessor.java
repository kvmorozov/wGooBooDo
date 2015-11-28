package ru.simpleGBD.App.Logic.Runtime;

import com.fasterxml.jackson.core.JsonParseException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import ru.simpleGBD.App.Logic.DataModel.PageInfo;
import ru.simpleGBD.App.Logic.DataModel.PagesInfo;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Logic.Proxy.IProxyListProvider;
import ru.simpleGBD.App.Utils.HttpConnections;
import ru.simpleGBD.App.Utils.Mapper;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by km on 21.11.2015.
 */
public class PageSigProcessor implements Runnable {

    private static Logger logger = Logger.getLogger(PageSigProcessor.class.getName());

    private PageInfo page;
    private String rqUrl;

    public PageSigProcessor(PageInfo page) {
        this.page = page;

        rqUrl = ExecutionContext.baseUrl + ImageExtractor.PAGES_REQUEST_TEMPLATE.replace(ImageExtractor.RQ_PG_PLACEHOLDER, page.getPid());
    }

    private void getSigs(HttpHost proxy) {
        HttpClient instance = HttpConnections.INSTANCE.getClient(proxy);
        boolean sigFound = false;
        HttpResponse response;

        try {
            logger.finest(String.format("Started sig processing for %s", page.getPid()));

            response = instance.execute(new HttpGet(rqUrl));

            String respStr = IOUtils.toString(response.getEntity().getContent());
            PagesInfo framePages = Mapper.objectMapper.readValue(respStr, PagesInfo.class);

            for (PageInfo framePage : framePages.getPagesArray())
                if (framePage.getSrc() != null) {
                    PageInfo _page = ExecutionContext.bookInfo.getPages().getPageByPid(framePage.getPid());

                    _page.setSrc(framePage.getSrc());

                    if (_page.getSig() != null) {
                        sigFound = true;
                        _page.setUsedProxy(proxy);
                    }
                }
        } catch (JsonParseException jpe) {

        } catch (java.net.SocketTimeoutException ste) {

        } catch (EOFException eof) {
            eof.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (instance instanceof Closeable)
                try {
                    ((Closeable) instance).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            page.setSigChecked(true);

            logger.finest(String.format("Finished sig processing for %s; sig %s found.", page.getPid(), sigFound ? "" : " not "));
        }
    }

    @Override
    public void run() {
        // Без прокси
        getSigs(null);

        for (HttpHost proxy : IProxyListProvider.getInstance().getProxyList())
            if (proxy.getPort() > 0)
                getSigs(proxy);
    }
}
