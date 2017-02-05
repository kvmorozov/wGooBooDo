package ru.kmorozov.library.data.model.book;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by km on 26.12.2016.
 */

@Document
public class BookInfo {

    public enum BookFormat {
        PDF("pdf"),
        DJVU("djvu"),
        DOC("doc"),
        DOCX("docx"),
        UNKNOWN("");

        String ext;

        public String getExt() {
            return ext;
        }

        BookFormat(String ext) {
            this.ext = ext;
        }
    }

    BookFormat format;
    String fileName;

    public BookFormat getFormat() {
        return format;
    }

    public void setFormat(BookFormat format) {
        this.format = format;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
