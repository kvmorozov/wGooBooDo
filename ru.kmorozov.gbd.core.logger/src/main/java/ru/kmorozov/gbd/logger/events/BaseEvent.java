package ru.kmorozov.gbd.logger.events;

/**
 * Created by km on 15.12.2015.
 */
public class BaseEvent {

    private final String eventInfo;

    BaseEvent(String eventInfo) {
        this.eventInfo = eventInfo;
    }

    public String getEventInfo() {
        return this.eventInfo;
    }

    @Override
    public String toString() {
        return this.eventInfo;
    }
}
