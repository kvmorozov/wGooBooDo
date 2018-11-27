package ru.kmorozov.library.data.loader.processors.gbd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.kmorozov.db.core.config.IContextLoader;
import ru.kmorozov.db.core.logic.model.book.BookInfo;
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleBookInfoExtractor;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.client.OneDriveProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class OneDriveContextLoader implements IContextLoader {

    protected static final Logger logger = Logger.getLogger(OneDriveContextLoader.class);

    @Autowired
    @Lazy
    private OneDriveProvider api;

    @Autowired
    @Lazy
    private DbContextLoader dbContextLoader;

    private Map<String, BookInfo> booksMap;
    private Map<String, OneDriveItem> itemsMap;

    void initContext(final OneDriveItem root) {
        this.booksMap = new HashMap<>();
        this.itemsMap = new HashMap<>();

        try {
            for (final OneDriveItem item : this.api.getChildren(root))
                if (item.isDirectory()) {
                    final String[] nameTokens = item.getName().split(" ");
                    final String bookId = nameTokens[nameTokens.length - 1];
                    this.booksMap.put(bookId, null);
                    this.itemsMap.put(bookId, item);
                }
        } catch (final IOException e) {
            OneDriveContextLoader.logger.error("Cannot init OneDriveContextLoader", e);
        }
    }

    @Override
    public void updateIndex() {

    }

    @Override
    public void updateContext() {

    }

    @Override
    public void updateBookInfo(final BookInfo bookInfo) {
        this.dbContextLoader.updateBookInfo(bookInfo);
    }

    @Override
    public BookInfo getBookInfo(final String bookId) {
        BookInfo bookInfo = this.booksMap.get(bookId);
        if (bookInfo == null)
            this.booksMap.put(bookId, bookInfo = (new GoogleBookInfoExtractor(bookId, this.dbContextLoader)).getBookInfo());

        return bookInfo;
    }

    public OneDriveItem getBookDir(final String bookId) {
        return this.itemsMap.get(bookId);
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
        if (this.api == null)
            return false;

        try {
            return this.api.getRoot() != null;
        } catch (final IOException e) {
            OneDriveContextLoader.logger.error("Invalid OneDrive connection", e);
            return false;
        }
    }
}
