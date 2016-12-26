package ru.kmorozov.library.data.model.book;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by km on 26.12.2016.
 */

@Document
public class BookInfo {

    public enum BookFormat {
        PDF("pdf"),
        DJVU("djvu");

        String ext;

        BookFormat(String ext) {
            this.ext = ext;
        }
    }

    BookFormat format;
}
