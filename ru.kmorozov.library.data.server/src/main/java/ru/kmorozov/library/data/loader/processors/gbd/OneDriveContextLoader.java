package ru.kmorozov.library.data.loader.processors.gbd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.core.config.IBaseLoader;
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleBookInfoExtractor;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.client.OneDriveProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class OneDriveContextLoader implements IBaseLoader {

    protected static final Logger logger = Logger.getLogger(OneDriveContextLoader.class);

    @Autowired
    @Lazy
    private OneDriveProvider api;

    @Autowired
    @Lazy
    private DbContextLoader dbContextLoader;

    private Map<String, BookInfo> booksMap;
    private Map<String, OneDriveItem> itemsMap;

    void initContext(OneDriveItem root) {
        booksMap = new HashMap<>();
        itemsMap = new HashMap<>();

        try {
            for (OneDriveItem item : api.getChildren(root))
                if (item.isDirectory()) {
                    String[] nameTokens = item.getName().split(" ");
                    String bookId = nameTokens[nameTokens.length - 1];
                    booksMap.put(bookId, null);
                    itemsMap.put(bookId, item);
                }
        } catch (IOException e) {
            logger.error("Cannot init OneDriveContextLoader", e);
        }
    }

    @Override
    public void updateIndex() {

    }

    @Override
    public void updateContext() {

    }

    @Override
    public void updateBookInfo(BookInfo bookInfo) {
        dbContextLoader.updateBookInfo(bookInfo);
    }

    @Override
    public BookInfo getBookInfo(String bookId) {
        BookInfo bookInfo = booksMap.get(bookId);
        if (bookInfo == null)
            booksMap.put(bookId, bookInfo = (new GoogleBookInfoExtractor(bookId, dbContextLoader)).getBookInfo());

        return bookInfo;
    }

    public OneDriveItem getBookDir(String bookId) {
        return itemsMap.get(bookId);
    }

    @Override
    public Set<String> getBookIdsList() {
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
        if (api == null)
            return false;

        try {
            return api.getRoot() != null;
        } catch (IOException e) {
            logger.error("Invalid OneDrive connection", e);
            return false;
        }
    }
}
