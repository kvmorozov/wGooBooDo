package ru.kmorozov.library.data.loader;

import com.wouterbreukink.onedrive.client.OneDriveProvider;
import com.wouterbreukink.onedrive.client.authoriser.AuthorisationProvider;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */

@Component
public class OneDriveLoader extends BaseLoader {

    private static final Logger logger = Logger.getLogger(OneDriveLoader.class);
    private final OneDriveProvider api;

    public OneDriveLoader(@Autowired String oneDriveKeyFileName) {
        File file = new File(getClass().getClassLoader().getResource(oneDriveKeyFileName).getFile());

        AuthorisationProvider authoriser = null;

        try {
            authoriser = AuthorisationProvider.FACTORY.create(file.toPath());
        } catch (IOException e) {
            logger.error("OneDrive API init error", e);
        }

        api = OneDriveProvider.FACTORY.readOnlyApi(authoriser);
    }

    @Override
    public void load() throws IOException {

    }
}
