package ru.kmorozov.library.data.loader.utils;

import com.wouterbreukink.onedrive.client.OneDriveUrl;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.dto.BookDTO;

/**
 * Created by sbt-morozov-kv on 14.04.2017.
 */
public class BookUtils {

    public static BookDTO createBookDIO(Book book) {
        BookDTO dto = new BookDTO(book);
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
}
