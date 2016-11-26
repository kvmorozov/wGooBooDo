package ru.kmorozov.gbd.core.config.storage;

import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;
import ru.kmorozov.gbd.core.utils.gson.Mapper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sbt-morozov-kv on 14.11.2016.
 */
public class BookContextLoader extends BaseLoader {

    private static final String CTX_FILE_NAME = "books.ctx";

    public static final BookContextLoader BOOK_CTX_LOADER = new BookContextLoader();

    private Map<String, BookInfo> booksInfo = new HashMap<>();

    protected BookContextLoader() {
        super();

        initContext();
    }

    @Override
    protected String getLoadedFileName() {
        return CTX_FILE_NAME;
    }

    public void updateContext() {
        List<BookInfo> runtimeBooksInfo = ExecutionContext.INSTANCE.getContexts(false).stream().map(BookContext::getBookInfo).collect(Collectors.toList());
        for (BookInfo bookInfo : runtimeBooksInfo)
            booksInfo.put(bookInfo.getBookId(), bookInfo);

        try {
            try (FileWriter writer = new FileWriter(getFileToLoad(true))) {
                Mapper.getGson().toJson(new ArrayList<>(booksInfo.values()), writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initContext() {
        if (!isValidDirectory())
            return;

        refreshContext();
    }

    public BookInfo getBookInfo(String bookId) {
        return booksInfo.get(bookId);
    }

    public int getContextSize() {
        return booksInfo.size();
    }

    public void refreshContext() {
        File contextFile = getFileToLoad(false);
        if (contextFile == null)
            return;

        try {
            BookInfo[] ctxObjArr;
            try (FileReader reader = new FileReader(contextFile)) {
                ctxObjArr = Mapper.getGson().fromJson(reader, BookInfo[].class);
            }
            for (Object ctxObj : ctxObjArr)
                if (ctxObj instanceof BookInfo) {
                    BookInfo bookInfo = (BookInfo) ctxObj;
                    booksInfo.put(bookInfo.getBookId(), bookInfo);
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
