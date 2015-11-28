package ru.simpleGBD.App.Logic.Runtime;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import ru.simpleGBD.App.Logic.DataModel.PageInfo;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Utils.HttpConnections;

import java.io.*;
import java.util.logging.Logger;

/**
 * Created by km on 21.11.2015.
 */
public class PageImgProcessor implements Runnable {

    private static Logger logger = Logger.getLogger(PageImgProcessor.class.getName());

    private PageInfo page;

    public PageImgProcessor(PageInfo page) {
        this.page = page;
    }

    @Override
    public void run() {
        // Залочено по ошибке, разлочиваем
        if (page.getSig() == null || !page.imgRequestLock.tryLock()) {
            try {
                page.imgRequestLock.unlock();
            }
            catch(Exception ex) {
                return;
            }
            return;
        }

        File outputFile = new File(ExecutionContext.outputDir.getPath() + "\\" + page.getPid() + ".png");
        if (outputFile.exists())
            return;

        InputStream inputStream = null;
        OutputStream outputStream = null;

        HttpClient instance = HttpClients
                .custom()
                .setUserAgent(ImageExtractor.USER_AGENT)
                .setDefaultCookieStore(HttpConnections.INSTANCE.getCookieStore()).build();

        try {
            logger.info(String.format("Started img processing for %s", page.getPid()));

            String imgUrl = ExecutionContext.baseUrl + ImageExtractor.IMG_REQUEST_TEMPLATE
                    .replace(ImageExtractor.RQ_PG_PLACEHOLDER, page.getPid())
                    .replace(ImageExtractor.RQ_SIG_PLACEHOLDER, page.getSig())
                    .replace(ImageExtractor.RQ_WIDTH_PLACEHOLDER, "800")
                    .replace("%WIDTH%", "800");

            HttpResponse response = instance.execute(new HttpGet(imgUrl));

            inputStream = response.getEntity().getContent();

            outputStream = new FileOutputStream(outputFile);
            int read = 0;
            byte[] bytes = new byte[4096];

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

            if (instance instanceof Closeable)
                try {
                    ((Closeable) instance).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            logger.info(String.format("Finished img processing for %s", page.getPid()));
        }
    }
}
