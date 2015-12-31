package ru.simpleGBD.App.Utils;

import com.google.api.client.http.HttpResponse;
import org.apache.commons.io.FilenameUtils;
import ru.simpleGBD.App.Logic.ExecutionContext;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * Created by km on 27.12.2015.
 */
public class Images {

    private static Logger logger = Logger.getLogger(ExecutionContext.output, Images.class.getName());

    public static boolean isImageFile(Path filePath) {
        if (!Files.isRegularFile(filePath))
            return false;

        String ext = FilenameUtils.getExtension(filePath.toString()).toLowerCase();

        switch (ext) {
            case "png":
            case "jpg":
            case "jpeg":
                return true;
            default:
                logger.severe(String.format("Unknown img format = %s", ext));
                return false;
        }
    }

    public static String getImageFormat(InputStream inputStream) {
        ImageInputStream iis = null;

        try {
            iis = ImageIO.createImageInputStream(inputStream);

            Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(iis);

            while (imageReaders.hasNext()) {
                ImageReader reader = (ImageReader) imageReaders.next();
                System.out.printf("formatName: %s%n", reader.getFormatName());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (iis != null)
                try {
                    iis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        return null;
    }

    public static String getImageFormat(HttpResponse response) {
        return response.getMediaType().getSubType();
    }
}