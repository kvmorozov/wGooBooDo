package ru.kmorozov.gbd.core.loader;

import ru.kmorozov.db.core.config.IContextLoader;
import ru.kmorozov.db.core.logic.model.book.BookInfo;
import ru.kmorozov.gbd.core.logic.context.IBookListProducer;
import ru.kmorozov.gbd.core.logic.library.LibraryFactory;

import java.util.Set;

public class ListBasedContextLoader implements IContextLoader {

    private final IBookListProducer producer;

    public ListBasedContextLoader(final IBookListProducer producer) {
        this.producer = producer;
    }

    @Override
    public void updateIndex() {

    }

    @Override
    public void updateContext() {

    }

    @Override
    public void updateBookInfo(final BookInfo bookInfo) {

    }

    @Override
    public BookInfo getBookInfo(final String bookId) {
        return LibraryFactory.getMetadata(bookId).getBookExtractor(bookId, null).getBookInfo();
    }

    @Override
    public Set<String> getBookIdsList() {
        return this.producer.getBookIds();
    }

    @Override
    public int getContextSize() {
        return 0;
    }

    @Override
    public void refreshContext() {

    }

    @Override
    public boolean isValid() {
        return true;
    }
}
