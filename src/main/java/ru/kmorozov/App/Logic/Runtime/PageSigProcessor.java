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

import java.io.*;
import java.util.logging.Logger;

/**
 * Created by km on 21.11.2015.
 */
public class PageSigProcessor implements Runnable {

    private static Logger logger = Logger.getLogger(PageSigProcessor.class.getName());

    private PageInfo page;
    private File dir;
    private String baseUrl;
    private PagesInfo bookInfo;

    public PageSigProcessor(PagesInfo bookInfo, String baseUrl, PageInfo page, File dir) {
        this.page = page;
        this.dir = dir;
        this.baseUrl = baseUrl;
        this.bookInfo = bookInfo;
    }

    @Override
    public void run() {
        String rqUrl = baseUrl + ImageExtractor.PAGES_REQUEST_TEMPLATE.replace(ImageExtractor.RQ_PG_PLACEHOLED, page.getPid());

        HttpClient instance = HttpClients.custom().setUserAgent(ImageExtractor.USER_AGENT).build();
        boolean sigFound = false;

        try {
            logger.info("Started sig processing for " + page.getPid());

            HttpResponse response = instance.execute(new HttpGet(rqUrl));

            String respStr = IOUtils.toString(response.getEntity().getContent());
            PagesInfo framePages = Mapper.objectMapper.readValue(respStr, PagesInfo.class);

            for (PageInfo framePage : framePages.getPages())
                if (framePage.getSrc() != null) {
                    PageInfo _page = bookInfo.getPageByPid(framePage.getPid());

                    // URL картинки известен и кто-то его уже грузит
                    if (_page.getSrc() != null && !_page.imgRequestLock.tryLock())
                        continue;

                    // Мы уже залочили страницу
                    _page.setSrc(framePage.getSrc());

                    if (_page.getSig() != null) {
                        sigFound = true;
                        synchronized(_page) {
                            Pools.imgExecutor.execute(new PageImgProcessor(bookInfo, baseUrl, page, dir));
//                            (new PageImgProcessor(bookInfo, baseUrl, page, dir)).run();
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

            logger.info("Finished sig processing for " + page.getPid() + "; sig " + (sigFound ? "" : " not " ) + "found.");
        }
    }
}
