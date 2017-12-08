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

        final String ext;

        public String getExt() {
            return ext;
        }

        BookFormat(final String ext) {
            this.ext = ext;
        }
    }

    BookFormat format;

    String fileName;

    Date lastModifiedDateTime;

    @Indexed(unique = true)
    String path;

    long size;

    public BookFormat getFormat() {
        return format;
    }

    public void setFormat(final BookFormat format) {
        this.format = format;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    boolean isLink() {return BookFormat.LNK == format;}

    public Date getLastModifiedDateTime() {
        return lastModifiedDateTime;
    }

    public void setLastModifiedDateTime(final Date lastModifiedDateTime) {
        this.lastModifiedDateTime = lastModifiedDateTime;
    }

    public long getSize() {
        return size;
    }

    public void setSize(final long size) {
        this.size = size;
    }
}
