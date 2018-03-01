package ru.kmorozov.library.data.loader.processors;

import com.kmorozov.onedrive.client.OneDriveItem;
import com.kmorozov.onedrive.client.OneDriveProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.core.loader.BookListLoader;
import ru.kmorozov.gbd.logger.Logger;

import java.io.IOException;

@Component
public class GbdProcessor implements IProcessor {

    protected static final Logger logger = Logger.getLogger(GbdProcessor.class);

    @Autowired
    private OneDriveProvider api;

    @Override
    public void process() {
        OneDriveItem gbdRoot = getGbdRoot();
        if (gbdRoot == null) {
            logger.error("GBD root not found, exiting.");
            return;
        }
    }

    private OneDriveItem getGbdRoot() {
        try {
            OneDriveItem[] searchResults = api.search(BookListLoader.INDEX_FILE_NAME);
            if (searchResults != null && searchResults.length == 1)
                return searchResults[0];
            else
                logger.error("Cannot find GBD root!");
        } catch (IOException e) {
            logger.error("Search error", e);
        }

        return null;
    }
}
