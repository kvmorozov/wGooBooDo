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

import java.io.*;

/**
 * Created by km on 21.11.2015.
 */
public class PageImgProcessor implements Runnable {

    private PageInfo page;
    private File dir;
    private String baseUrl;
    private PagesInfo bookInfo;
    private HttpClient instance;

    public PageImgProcessor(PagesInfo bookInfo, String baseUrl, PageInfo page, File dir) {
        this.page = page;
        this.dir = dir;
        this.baseUrl = baseUrl;
        this.bookInfo = bookInfo;

        instance = HttpClients.custom().setUserAgent(ImageExtractor.USER_AGENT).build();
    }

    @Override
    public void run() {
        String imgUrl = baseUrl + ImageExtractor.IMG_REQUEST_TEMPLATE
                .replace(ImageExtractor.RQ_PG_PLACEHOLED, page.getPid())
                .replace(ImageExtractor.RQ_SIG_PLACEHOLED, page.getSig());


        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            HttpResponse response = instance.execute(new HttpGet(imgUrl));

            inputStream = response.getEntity().getContent();

            outputStream = new FileOutputStream(new File(dir.getPath() + "\\" + page.getPid() + ".png"));
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
