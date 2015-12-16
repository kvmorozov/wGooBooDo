package ru.simpleGBD.App.Logic.Output.events;

import java.util.logging.Level;

/**
 * Created by km on 15.12.2015.
 */
public class LogEvent extends BaseEvent {

    private Level level;

    public LogEvent(Level level, String eventInfo) {
        super(eventInfo);

        this.level = level;
    }

    public Level getLevel() {
        return level;
    }
}
