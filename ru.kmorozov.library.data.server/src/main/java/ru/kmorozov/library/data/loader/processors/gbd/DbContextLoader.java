package ru.kmorozov.library.data.loader.processors.gbd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.core.config.IBaseLoader;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;
import ru.kmorozov.library.data.repository.GoogleBooksRepository;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DbContextLoader implements IBaseLoader {

    private Map<String, BookInfo> booksMap;

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
        return booksMap.get(bookId);
    }

    @Override
    public Set<String> getBookIdsList() {
        if (booksMap == null) {
            booksMap = googleBooksRepository.findAll().stream().collect(Collectors.toMap(BookInfo::getBookId, s -> s));
        }

        return booksMap.keySet();
    }

    @Override
    public int getContextSize() {
        return booksMap.size();
    }

    @Override
    public void refreshContext() {

    }

    @Override
    public boolean isValid() {
        return false;
    }
}
