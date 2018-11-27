package ru.kmorozov.library.data.server.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.library.data.loader.impl.OneDriveLoader;
import ru.kmorozov.library.data.server.condition.StorageEnabledCondition;

import java.io.IOException;

/**
 * Created by sbt-morozov-kv on 13.04.2017.
 */
@Component
@ComponentScan(basePackageClasses = OneDriveLoader.class)
@Conditional(StorageEnabledCondition.class)
public class ScheduledOneDriveTasks {

    private static final long SCHEDULE_INTERVAL = 1 * 60 * 60 * 1000;
    private static final Logger logger = Logger.getLogger(ScheduledOneDriveTasks.class);

    @Autowired
    @Lazy
    private OneDriveLoader oneLoader;

    @Value("${onedrive.scheduler.enabled}")
    private boolean schedulerEnabled;

    @Scheduled(fixedRate = ScheduledOneDriveTasks.SCHEDULE_INTERVAL)
    public void refreshOneDrive() throws IOException {
        if (!this.schedulerEnabled)
            return;

        ScheduledOneDriveTasks.logger.info("Scheduled refresh started");
        this.oneLoader.load(oneDriveItem -> Long.MAX_VALUE < System.currentTimeMillis() - oneDriveItem.getLastModifiedDateTime().getTime());
        ScheduledOneDriveTasks.logger.info("Scheduled refresh finished");
    }

    @Scheduled(fixedRate = ScheduledOneDriveTasks.SCHEDULE_INTERVAL)
    public void processLinks() throws IOException {
        if (!this.schedulerEnabled)
            return;

        ScheduledOneDriveTasks.logger.info("Scheduled links processing started");
        this.oneLoader.processLinks();
        ScheduledOneDriveTasks.logger.info("Scheduled links processing finished");
    }
}
