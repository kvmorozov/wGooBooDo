package ru.kmorozov.library.data.model.book;

import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;

/**
 * Created by km on 26.12.2016.
 */

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

    String fileName;

    Date lastModifiedDateTime;

    @Indexed(unique = true)
    String path;

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

    boolean isLink() {return format == BookFormat.LNK;}

    public Date getLastModifiedDateTime() {
        return lastModifiedDateTime;
    }

    public void setLastModifiedDateTime(Date lastModifiedDateTime) {
        this.lastModifiedDateTime = lastModifiedDateTime;
    }
}
