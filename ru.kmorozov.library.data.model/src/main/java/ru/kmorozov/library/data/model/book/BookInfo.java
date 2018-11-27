package ru.kmorozov.library.data.model.book;

import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;
import java.util.Map;

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
            return this.ext;
        }

        BookFormat(String ext) {
            this.ext = ext;
        }
    }

    public enum BookType {
        ARTICLE,
        GOOGLE_BOOK
    }

    private String bookId;

    private BookInfo.BookFormat format;

    private BookInfo.BookType bookType;

    private String fileName;

    private Date lastModifiedDateTime;

    @Indexed(unique = true)
    private String path;

    long size;

    private Map<String, String> customFields;

    public String getBookId() {
        return bookId;
    }

    public void setBookId(final String bookId) {
        this.bookId = bookId;
    }

    public BookInfo.BookFormat getFormat() {
        return this.format;
    }

    public void setFormat(BookInfo.BookFormat format) {
        this.format = format;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    boolean isLink() {
        return BookInfo.BookFormat.LNK == this.format;
    }

    public Date getLastModifiedDateTime() {
        return this.lastModifiedDateTime;
    }

    public void setLastModifiedDateTime(Date lastModifiedDateTime) {
        this.lastModifiedDateTime = lastModifiedDateTime;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public BookInfo.BookType getBookType() {
        return bookType;
    }

    public void setBookType(final BookInfo.BookType bookType) {
        this.bookType = bookType;
    }

    public Map<String, String> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(final Map<String, String> customFields) {
        if (this.customFields == null || this.customFields.isEmpty())
            this.customFields = customFields;
        else
            this.customFields.putAll(customFields);
    }
}
