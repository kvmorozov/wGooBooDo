package ru.simpleGBD.App.Logic.Output.events;

/**
 * Created by km on 15.12.2015.
 */
public class BaseEvent {

    private final String eventInfo;

    BaseEvent(String eventInfo) {
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
