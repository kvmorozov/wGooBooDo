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
        LNK("lnk"),
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
    String fileName, path;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isLink() {return format == BookFormat.LNK;}
}
