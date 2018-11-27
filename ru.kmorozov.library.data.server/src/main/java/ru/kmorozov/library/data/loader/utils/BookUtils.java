package ru.kmorozov.library.data.loader.utils;

import ru.kmorozov.onedrive.client.OneDriveUrl;
import org.springframework.util.StringUtils;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.dto.BookDTO;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by sbt-morozov-kv on 14.04.2017.
 */
public class BookUtils {

    public static BookDTO createBookDIO(final Book book) {
        final BookDTO dto = new BookDTO(book, bookLoaded(book));
        switch (book.getStorage().getStorageType()) {
            case LocalFileSystem:
                dto.setPath(book.getBookInfo().getPath());
                break;
            case OneDrive:
                dto.setPath(OneDriveUrl.content(book.getBookInfo().getPath()).toString());
                break;
        }

        return dto;
    }

    public static boolean bookLoaded(final Book book) {
        final String bookPath = book.getStorage().getLocalPath() + File.separator + book.getBookInfo().getFileName();
        if (!StringUtils.isEmpty(bookPath)) {
            return Files.exists(Paths.get(bookPath));
        }

        return false;
    }
}
