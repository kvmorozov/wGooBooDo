package ru.kmorozov.library.data.loader.processors.gbd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.kmorozov.db.core.config.IContextLoader;
import ru.kmorozov.db.core.logic.model.book.BookInfo;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.library.data.repository.GoogleBooksRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class DbContextLoader implements IContextLoader {

    private final Map<String, BookInfo> booksMap = new HashMap<>();

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
    public void updateBookInfo(final BookInfo bookInfo) {
        final BookInfo existBookInfo = this.googleBooksRepository.findByBookId(bookInfo.getBookId());
        if (existBookInfo != null)
            this.googleBooksRepository.delete(existBookInfo);

        this.googleBooksRepository.save(bookInfo);
    }

    @Override
    public BookInfo getBookInfo(final String bookId) {
        BookInfo info = this.booksMap.get(bookId);
        if (info == null) {
            info = this.googleBooksRepository.findByBookId(bookId);

            for (final IPage page : info.getPages().getPages())
                ((AbstractPage) page).setFileExists(true);

            this.booksMap.put(bookId, info);
        }
        return info;
    }

    @Override
    public Set<String> getBookIdsList() {
        return this.booksMap.keySet();
    }

    @Override
    public int getContextSize() {
        return this.booksMap.size();
    }

    @Override
    public void refreshContext() {

    }

    @Override
    public boolean isValid() {
        return false;
    }

}
