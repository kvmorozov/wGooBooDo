package ru.kmorozov.gbd.core.producers;

import ru.kmorozov.gbd.core.logic.context.IBookListProducer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by km on 12.11.2016.
 */
public class SingleBookProducer implements IBookListProducer {

    private final Set<String> singletonSet;

    public SingleBookProducer(String bookId) {
        this.singletonSet = new HashSet<>(Collections.singletonList(bookId));
    }

    @Override
    public Set<String> getBookIds() {
        return this.singletonSet;
    }
}
