package ru.kmorozov.library.data.loader;

import com.wouterbreukink.onedrive.tasks.TaskReporter;
import org.apache.log4j.Logger;
import ru.kmorozov.library.data.loader.netty.EventSender;

/**
 * Created by sbt-morozov-kv on 22.06.2017.
 */
public class SocketReporter extends TaskReporter {

    private static final Logger logger = Logger.getLogger(SocketReporter.class);

    @Override
    public void info(String message) {
        EventSender.INSTANCE.sendInfo(logger, message);
    }

    @Override
    public void warn(String message) {
        EventSender.INSTANCE.sendInfo(logger, message);
    }
}
