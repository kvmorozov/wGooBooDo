package ru.kmorozov.gbd.core.logic.output.consumers;

import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;
import ru.kmorozov.gbd.core.logic.output.listeners.DummyLogEventListener;

/**
 * Created by km on 13.12.2015.
 */
public class DummyBookInfoOutput extends AbstractOutput {

    public DummyBookInfoOutput() {
        addListener(new DummyLogEventListener());
    }

    @Override
    public void receiveBookInfo(final BookInfo bookInfo) {

    }
}
