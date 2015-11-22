package ru.kmorozov.App.Logic.Runtime;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import ru.kmorozov.App.Logic.DataModel.PageInfo;
import ru.kmorozov.App.Logic.DataModel.PagesInfo;
import ru.kmorozov.App.Logic.ExecutionContext;
import ru.kmorozov.App.Utils.Mapper;
import ru.kmorozov.App.Utils.Pools;

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

    public PageSigProcessor(PageInfo page) {
        this.page = page;
    }

    @Override
    public void run() {
        String rqUrl = ExecutionContext.baseUrl + ImageExtractor.PAGES_REQUEST_TEMPLATE.replace(ImageExtractor.RQ_PG_PLACEHOLED, page.getPid());

        HttpClient instance = HttpClients.custom().setUserAgent(ImageExtractor.USER_AGENT).build();
        boolean sigFound = false;

        try {
            logger.info(String.format("Started sig processing for %s", page.getPid()));

            HttpResponse response = instance.execute(new HttpGet(rqUrl));

            String respStr = IOUtils.toString(response.getEntity().getContent());
            PagesInfo framePages = Mapper.objectMapper.readValue(respStr, PagesInfo.class);

            for (PageInfo framePage : framePages.getPages())
                if (framePage.getSrc() != null) {
                    PageInfo _page = ExecutionContext.bookInfo.getPageByPid(framePage.getPid());

                    // URL картинки известен и кто-то его уже грузит
                    if (_page.getSrc() != null && !_page.imgRequestLock.tryLock())
                        continue;

                    // Мы уже залочили страницу
                    _page.setSrc(framePage.getSrc());

                    if (_page.getSig() != null) {
                        sigFound = true;
                        synchronized(_page) {
//                            Pools.imgExecutor.execute(new PageImgProcessor(page));
                        }
                    }
                }
        } catch (EOFException eof) {
            eof.printStackTrace();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if (instance instanceof Closeable)
                try {
                    ((Closeable) instance).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            page.setSigChecked(true);

            logger.info(String.format("Finished sig processing for %s; sig %s found.", page.getPid(), sigFound ? "" : " not "));
        }
    }
}
