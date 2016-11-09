package ru.kmorozov.gbd.core.logic.output.consumers;

import ru.kmorozov.gbd.core.logic.model.book.BookInfo;
import ru.kmorozov.gbd.core.logic.output.listeners.DummyLogEventListener;

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