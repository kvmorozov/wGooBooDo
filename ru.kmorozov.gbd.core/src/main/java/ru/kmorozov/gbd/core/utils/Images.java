package ru.kmorozov.gbd.core.utils;

import org.apache.commons.io.FilenameUtils;
import ru.kmorozov.gbd.core.logic.connectors.Response;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by km on 27.12.2015.
 */
public class Images {

    private static final Set<Long> GOOGLE_BAD_FILES_SIZES = new HashSet(Arrays.asList(96352L, 96183L));

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
                logger.severe(String.format("Unknown img format = %s", ext));
                return false;
        }
    }

    public static boolean isValidImage(Path filePath) {
        Long fileSize = filePath.toFile().length();
        return !GOOGLE_BAD_FILES_SIZES.contains(fileSize);
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
