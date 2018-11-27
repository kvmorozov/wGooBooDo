package ru.kmorozov.gbd.utils;

import org.apache.commons.io.FilenameUtils;
import ru.kmorozov.gbd.core.logic.connectors.Response;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.logger.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by km on 27.12.2015.
 */
public class Images {

    private static final Logger logger = ExecutionContext.INSTANCE.getLogger(Images.class);

    public static boolean isImageFile(Path filePath) {
        if (!Files.isRegularFile(filePath)) return false;

        String ext = FilenameUtils.getExtension(filePath.toString()).toLowerCase();

        switch (ext) {
            case "png":
            case "jpg":
            case "jpeg":
                return true;
            case "pdf":
                return false;
            default:
                Images.logger.severe(String.format("Unknown img format = %s", ext));
                return false;
        }
    }

    public static boolean isInvalidImage(Path filePath, int imgWidth) {
        return Images.isInvalidImage(filePath.toFile(), imgWidth);
    }

    public static boolean isInvalidImage(File imgfile, int imgWidth) {
        Long fileSize = imgfile.length();

        switch (imgWidth) {
            case 1280:
                if (96183L <= fileSize && 97200L > fileSize) {
                    try {
                        BufferedImage bimg = ImageIO.read(imgfile);
                        return 1670 == bimg.getHeight();
                    } catch (IOException e) {
                        return true;
                    }
                }
                else
                    return false;
            default:
                return false;
        }
    }

    public static boolean isPdfFile(Path filePath) {
        if (!Files.isRegularFile(filePath)) return false;

        String ext = FilenameUtils.getExtension(filePath.toString()).toLowerCase();

        return "pdf".equals(ext);
    }

    public static String getImageFormat(Response response) {
        return response.getImageFormat();
    }
}
