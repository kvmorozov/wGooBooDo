package ru.kmorozov.library.data.loader.processors.gbd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.core.config.IBaseLoader;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;
import ru.kmorozov.library.data.repository.GoogleBooksRepository;

import java.util.Set;

@Component
public class DbContextLoader implements IBaseLoader {

    @Autowired
    @Lazy
    private GoogleBooksRepository googleBooksRepository;

    @Override
    public void updateIndex() {

    }

    @Override
    public void updateContext() {

    }

    @Override
    public void updateBookInfo(BookInfo bookInfo) {
        BookInfo existBookInfo = googleBooksRepository.findByBookId(bookInfo.getBookId());
        if (existBookInfo != null)
            googleBooksRepository.delete(existBookInfo);

        googleBooksRepository.save(bookInfo);
    }

    @Override
    public BookInfo getBookInfo(String bookId) {
        return googleBooksRepository.findByBookId(bookId);
    }

    @Override
    public Set<String> getBookIdsList() {
        return null;
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
        return false;
    }
}
