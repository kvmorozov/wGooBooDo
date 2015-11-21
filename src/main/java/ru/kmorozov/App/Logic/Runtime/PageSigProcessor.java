package ru.kmorozov.App.Logic.Runtime;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import ru.kmorozov.App.ImageExtractor;
import ru.kmorozov.App.Logic.DataModel.PageInfo;
import ru.kmorozov.App.Logic.DataModel.PagesInfo;
import ru.kmorozov.App.Utils.Mapper;
import ru.kmorozov.App.Utils.Pools;

import java.io.*;
import java.util.logging.Level;

/**
 * Created by km on 21.11.2015.
 */
public class PageSigProcessor implements Runnable {

    private PageInfo page;
    private File dir;
    private String baseUrl;
    private PagesInfo bookInfo;
    private HttpClient instance;

    public PageSigProcessor(PagesInfo bookInfo, String baseUrl, PageInfo page, File dir) {
        this.page = page;
        this.dir = dir;
        this.baseUrl = baseUrl;
        this.bookInfo = bookInfo;

        instance = HttpClients.custom().setUserAgent(ImageExtractor.USER_AGENT).build();
    }

    @Override
    public void run() {
        String rqUrl = baseUrl + ImageExtractor.PAGES_REQUEST_TEMPLATE.replace(ImageExtractor.RQ_PG_PLACEHOLED, page.getPid());

        try {
            HttpResponse response = instance.execute(new HttpGet(rqUrl));

            String respStr = IOUtils.toString(response.getEntity().getContent());
            PagesInfo framePages = Mapper.objectMapper.readValue(respStr, PagesInfo.class);

            for (PageInfo framePage : framePages.getPages())
                if (framePage.getSrc() != null) {
                    PageInfo _page = bookInfo.getPageByPid(framePage.getPid());
                    _page.setSrc(framePage.getSrc());
                    _page.imgRequestStarted.set(true);
                    Pools.imgExecutor.execute(new PageImgProcessor(bookInfo, baseUrl, page, dir));
                }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
