package ru.kmorozov.library.data.server;

import com.wouterbreukink.onedrive.client.OneDriveProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.kmorozov.library.data.loader.OneDriveLoader;

import java.io.IOException;

/**
 * Created by sbt-morozov-kv on 13.04.2017.
 */

@Component
public class ScheduledOneDriveTasks {

    @Autowired
    private OneDriveLoader oneLoader;

    @Scheduled(fixedRate = 1 * 60 * 60 * 1000)
    public void refreshOneDrive() {

    }

    @Scheduled(fixedRate = 1 * 60 * 60 * 1000)
    public void processLinks() throws IOException {
        oneLoader.processLinks();
    }
}
