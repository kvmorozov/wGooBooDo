package ru.simpleGBD.App.Logic.Output.events;

/**
 * Created by km on 15.12.2015.
 */
public class BaseEvent {

    protected String eventInfo;

    public BaseEvent(String eventInfo) {
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
