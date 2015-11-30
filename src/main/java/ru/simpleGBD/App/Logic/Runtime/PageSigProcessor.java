package ru.simpleGBD.App.Logic.Runtime;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import ru.simpleGBD.App.Logic.DataModel.PageInfo;
import ru.simpleGBD.App.Logic.DataModel.PagesInfo;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Logic.Proxy.HttpHostExt;
import ru.simpleGBD.App.Logic.Proxy.IProxyListProvider;
import ru.simpleGBD.App.Utils.HttpConnections;
import ru.simpleGBD.App.Utils.Mapper;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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

    private boolean getSigs(HttpHostExt proxy) {
        if (page.sigChecked.get())
            return true;

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
                        if (_page.getPid().equals(page.getPid()))
                            sigFound = true;
                        _page.setUsedProxy(proxy);
                        _page.sigChecked.set(true);
                    }
                }
        } catch (JsonParseException | JsonMappingException | SocketTimeoutException | SocketException | NoHttpResponseException ce) {
            if (proxy != null)
                proxy.setAvailable(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (instance instanceof Closeable)
                try {
                    ((Closeable) instance).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            logger.finest(String.format("Finished sig processing for %s; sig %s found.", page.getPid(), sigFound ? "" : " not "));
        }

        return sigFound;
    }

    @Override
    public void run() {
        if (page.getSig() != null)
            return;

        // Без прокси
        if (getSigs(null))
            return;

        for (HttpHostExt proxy : IProxyListProvider.getInstance().getProxyList())
            if (proxy.isAvailable() && proxy.getHost().getPort() > 0)
                if (getSigs(proxy))
                    return;
    }
}
