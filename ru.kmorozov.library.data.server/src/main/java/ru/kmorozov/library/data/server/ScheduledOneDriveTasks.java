package ru.kmorozov.library.data.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.library.data.loader.OneDriveLoader;

import java.io.IOException;

/**
 * Created by sbt-morozov-kv on 13.04.2017.
 */

@Component
public class ScheduledOneDriveTasks {

    private static final long SCHEDULE_INTERVAL = 1 * 60 * 60 * 1000;
    private static final Logger logger = Logger.getLogger(ScheduledOneDriveTasks.class);

    @Autowired
    private OneDriveLoader oneLoader;

    @Scheduled(fixedRate = SCHEDULE_INTERVAL)
    public void refreshOneDrive() throws IOException {
        logger.info("Scheduled refresh started");
        oneLoader.load(oneDriveItem -> Long.MAX_VALUE < System.currentTimeMillis() - oneDriveItem.getLastModifiedDateTime().getTime());
        logger.info("Scheduled refresh finished");
    }

    @Scheduled(fixedRate = SCHEDULE_INTERVAL)
    public void processLinks() throws IOException {
        logger.info("Scheduled links processing started");
        oneLoader.processLinks();
        logger.info("Scheduled links processing finished");
    }
}
