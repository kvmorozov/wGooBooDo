package ru.kmorozov.gbd.core.config.storage;

import ru.kmorozov.gbd.core.config.GBDOptions;

import java.io.File;
import java.io.IOException;

/**
 * Created by sbt-morozov-kv on 14.11.2016.
 */
public abstract class BaseLoader {

    protected BaseLoader() {

    }

    protected abstract String getLoadedFileName();

    protected File getFileToLoad(boolean createIfNotExists) {
        if (!GBDOptions.isValidDirectory())
            return null;

        File indexFile = new File(GBDOptions.getOutputDir() + File.separator + getLoadedFileName());
        if (indexFile.exists())
            return indexFile;
        else if (createIfNotExists) {
            try {
                indexFile.createNewFile();
                return indexFile;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
