package ru.kmorozov.gbd.logger.events;

import java.util.logging.Level;

/**
 * Created by km on 15.12.2015.
 */
public class LogEvent extends BaseEvent {

    private final Level level;

    public LogEvent(final Level level, final String eventInfo) {
        super(eventInfo);

        this.level = level;
    }

    public Level getLevel() {
        return level;
    }
}
