package ru.simpleGBD.App.Logic.Runtime;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
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
            } catch (Exception ex) {
                return;
            }
            return;
        }

        File outputFile = new File(ExecutionContext.outputDir.getPath() + "\\" + page.getOrder() + "_" + page.getPid() + ".png");
        if (outputFile.exists())
            return;

        InputStream inputStream = null;
        OutputStream outputStream = null;

        HttpClientBuilder instanceBuilder = HttpConnections.INSTANCE
                .getBuilder();

        if (page.getUsedProxy() != null)
            instanceBuilder.setProxy(page.getUsedProxy());

        HttpClient instance = instanceBuilder.build();

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
        } catch (Exception ex) {}
        finally {
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
