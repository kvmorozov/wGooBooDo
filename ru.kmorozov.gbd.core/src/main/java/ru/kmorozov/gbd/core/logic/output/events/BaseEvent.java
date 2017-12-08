package ru.kmorozov.gbd.core.logic.output.events;

/**
 * Created by km on 15.12.2015.
 */
public class BaseEvent {

    private final String eventInfo;

    BaseEvent(final String eventInfo) {
        this.eventInfo = eventInfo;
    }

    public String getEventInfo() {
        return eventInfo;
    }

    @Override
    public String toString() {
        return eventInfo;
    }
}
