package ru.kmorozov.gbd.desktop.library;

import ru.kmorozov.gbd.core.logic.context.IBookListProducer;

import java.util.Collections;
import java.util.List;

/**
 * Created by km on 12.11.2016.
 */
public class SingleBookProducer implements IBookListProducer {

    private List<String> singletonList;

    public SingleBookProducer(String bookId) {
        singletonList = Collections.singletonList(bookId);
    }

    @Override
    public List<String> getBookIds() {
        return singletonList;
    }
}
