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
import ru.simpleGBD.App.Utils.HttpConnections;
import ru.simpleGBD.App.Utils.Mapper;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

/**
 * Created by km on 21.11.2015.
 */
public class PageSigProcessor implements Runnable {

    private static Logger logger = Logger.getLogger(PageSigProcessor.class.getName());

    private HttpHostExt proxy;

    public PageSigProcessor(HttpHostExt proxy) {
        this.proxy = proxy;
    }

    private boolean getSig(PageInfo page) {
        boolean sigFound = false;
        HttpResponse response;
        String rqUrl = ExecutionContext.baseUrl + ImageExtractor.PAGES_REQUEST_TEMPLATE.replace(ImageExtractor.RQ_PG_PLACEHOLDER, page.getPid());
        HttpClient instance = HttpConnections.INSTANCE.getClient(proxy);

        try {
            logger.finest(String.format("Started sig processing for %s", page.getPid()));

            response = instance.execute(new HttpGet(rqUrl));

            String respStr = IOUtils.toString(response.getEntity().getContent());
            PagesInfo framePages = Mapper.objectMapper.readValue(respStr, PagesInfo.class);

            PageInfo[] pages = framePages.getPagesArray();
            for (PageInfo framePage : pages)
                if (framePage.getSrc() != null) {
                    PageInfo _page = ExecutionContext.bookInfo.getPagesInfo().getPageByPid(framePage.getPid());

                    if (_page.dataProcessed.get() || _page.getSig() != null || _page.sigChecked.get())
                        continue;

                    String _frameSrc = framePage.getSrc();
                    if (_frameSrc != null)
                        _page.setSrc(_frameSrc);

                    if (_page.getSig() != null) {
                        if (_page.getPid().equals(page.getPid()))
                            sigFound = true;
                        _page.setUsedProxy(proxy);
                        _page.sigChecked.set(true);
                    }
                }
        } catch (JsonParseException | JsonMappingException | SocketTimeoutException | SocketException | NoHttpResponseException ce) {
            if (proxy != null)
                proxy.registerFailure();
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
        if (proxy != null && !proxy.isAvailable())
            return;

        for (PageInfo page : ExecutionContext.bookInfo.getPagesInfo().getPages())
            if (!page.dataProcessed.get() && page.getSig() == null && !page.sigChecked.get()) {
                getSig(page);
            }
    }
}
