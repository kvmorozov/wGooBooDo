package ru.kmorozov.gbd.core.utils;

import org.apache.commons.io.FilenameUtils;
import ru.kmorozov.gbd.core.logic.ExecutionContext;
import ru.kmorozov.gbd.core.logic.connectors.Response;

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

    private static final Logger logger = Logger.getLogger(ExecutionContext.INSTANCE.getOutput(), Images.class.getName());

    public static boolean isImageFile(Path filePath) {
        if (!Files.isRegularFile(filePath))
            return false;

        String ext = FilenameUtils.getExtension(filePath.toString()).toLowerCase();

        switch (ext) {
            case "png":
            case "jpg":
            case "jpeg":
                return true;
            case "pdf":
                return false;
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
                ImageReader reader = imageReaders.next();
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

    public static String getImageFormat(Response response) {
        return response.getImageFormat();
    }
}
