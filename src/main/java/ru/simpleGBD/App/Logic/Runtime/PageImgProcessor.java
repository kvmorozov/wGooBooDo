package ru.simpleGBD.App.Logic.Runtime;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
    private static byte[] pngFormat = {(byte) 0x89, 0x50, 0x4e, 0x47};
    private static int dataChunk = 4096;

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

        File outputFile = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        HttpClient instance = HttpConnections.INSTANCE.getClient(page.getUsedProxy());

        try {
            String imgUrl = ExecutionContext.baseUrl + ImageExtractor.IMG_REQUEST_TEMPLATE
                    .replace(ImageExtractor.RQ_PG_PLACEHOLDER, page.getPid())
                    .replace(ImageExtractor.RQ_SIG_PLACEHOLDER, page.getSig())
                    .replace(ImageExtractor.RQ_WIDTH_PLACEHOLDER, "800");

            HttpResponse response = instance.execute(new HttpGet(imgUrl));

            inputStream = response.getEntity().getContent();

            int read = 0;
            byte[] bytes = new byte[dataChunk];
            boolean firstChunk = true;

            while ((read = inputStream.read(bytes)) != -1) {
                if (firstChunk) {
                    if (bytes[0] == pngFormat[0] && bytes[1] == pngFormat[1] && bytes[2] == pngFormat[2] && bytes[3] == pngFormat[3])
                        outputFile = new File(ExecutionContext.outputDir.getPath() + "\\" + page.getOrder() + "_" + page.getPid() + ".png");
                    else
                        outputFile = new File(ExecutionContext
                                .outputDir.getPath() + "\\" + page.getOrder() + "_" + page.getPid() + "_" + System.currentTimeMillis() + "err.txt");

                    if (outputFile != null && outputFile.exists())
                        break;
                    else
                        page.setDataProcessed(true);

                    logger.info(String.format("Started img processing for %s", page.getPid()));
                    outputStream = new FileOutputStream(outputFile);
                }

                firstChunk = false;

                outputStream.write(bytes, 0, read);
            }

            if (page.isDataProcessed())
                logger.info(String.format("Finished img processing for %s", page.getPid()));
        } catch (Exception ex) {
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
        }
    }
}
