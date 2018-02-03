package ru.kmorozov.gbd.logger.output;

import ru.kmorozov.gbd.logger.model.ILoggableObject;

/**
 * Created by km on 13.12.2015.
 */
@FunctionalInterface
public interface IOutputReceiver {

    void receive(ILoggableObject bookInfo);
}
