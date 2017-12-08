package ru.kmorozov.gbd.core.config.storage;

import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;
import ru.kmorozov.gbd.core.utils.gson.Mapper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sbt-morozov-kv on 14.11.2016.
 */
public class BookContextLoader extends BaseLoader {

    static final BookContextLoader BOOK_CTX_LOADER = new BookContextLoader();
    private static final String CTX_FILE_NAME = "books.ctx";
    private final Map<String, BookInfo> booksInfo = new HashMap<>();

    public BookContextLoader() {

        initContext();
    }

    @Override
    protected String getLoadedFileName() {
        return CTX_FILE_NAME;
    }

    public void updateContext() {
        if (!StringUtils.isEmpty(GBDOptions.getBookId())) return;

        final List<BookInfo> runtimeBooksInfo = ExecutionContext.INSTANCE.getContexts(false).stream().map(BookContext::getBookInfo).collect(Collectors.toList());
        for (final BookInfo bookInfo : runtimeBooksInfo)
            booksInfo.put(bookInfo.getBookId(), bookInfo);

        try {
            try (FileWriter writer = new FileWriter(getFileToLoad(true))) {
                Mapper.getGson().toJson(new ArrayList<>(booksInfo.values()), writer);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private void initContext() {
        if (!GBDOptions.isValidDirectory()) return;

        refreshContext();
    }

    public BookInfo getBookInfo(final String bookId) {
        return booksInfo.get(bookId);
    }

    public Iterable<BookInfo> getBooks() {
        return booksInfo.values();
    }

    public int getContextSize() {
        return booksInfo.size();
    }

    public void refreshContext() {
        final File contextFile = getFileToLoad(false);
        if (null == contextFile) return;

        try {
            final BookInfo[] ctxObjArr;
            try (FileReader reader = new FileReader(contextFile)) {
                ctxObjArr = Mapper.getGson().fromJson(reader, BookInfo[].class);
            }
            for (final Object ctxObj : ctxObjArr)
                if (ctxObj instanceof BookInfo) {
                    final BookInfo bookInfo = (BookInfo) ctxObj;
                    booksInfo.put(bookInfo.getBookId(), bookInfo);
                }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
