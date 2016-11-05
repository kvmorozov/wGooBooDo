package ru.kmorozov.gbd.core.logic.output.events;

import java.util.logging.Level;

/**
 * Created by km on 15.12.2015.
 */
public class LogEvent extends BaseEvent {

    private final Level level;

    public LogEvent(Level level, String eventInfo) {
        super(eventInfo);

        this.level = level;
    }

    public Level getLevel() {
        return level;
    }
}
