package ru.simpleGBD.App.Logic.Output.consumers;

import ru.simpleGBD.App.Logic.DataModel.BookInfo;
import ru.simpleGBD.App.Logic.Output.listeners.DummyLogEventListener;

/**
 * Created by km on 13.12.2015.
 */
public class DummyBookInfoOutput extends AbstractOutput {

    public DummyBookInfoOutput() {
        addListener(new DummyLogEventListener());
    }

    @Override
    public void receiveBookInfo(BookInfo bookInfo) {

    }
}
