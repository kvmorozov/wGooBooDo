package ru.kmorozov.gbd.logger.output;

import ru.kmorozov.gbd.logger.consumers.AbstractOutputReceiver;
import ru.kmorozov.gbd.logger.listeners.DummyLogEventListener;
import ru.kmorozov.gbd.logger.model.ILoggableObject;

/**
 * Created by km on 13.12.2015.
 */
public class DummyReceiver extends AbstractOutputReceiver {

    public DummyReceiver() {
        addListener(new DummyLogEventListener());
    }

    @Override
    public void receive(final ILoggableObject bookInfo) {

    }
}
