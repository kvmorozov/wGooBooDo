package ru.kmorozov.gbd.core.logic;

import ru.kmorozov.gbd.core.logic.model.book.BookInfo;
import ru.kmorozov.gbd.core.logic.output.consumers.AbstractOutput;

import java.io.File;

/**
 * Created by km on 22.11.2015.
 */
public class ExecutionContext {

    public static final ExecutionContext INSTANCE = new ExecutionContext();

    private ExecutionContext() {}

    private String bookId;
    private String baseUrl;
    private BookInfo bookInfo;
    private File outputDir;
    private AbstractOutput output;

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public BookInfo getBookInfo() {
        return bookInfo;
    }

    public void setBookInfo(BookInfo bookInfo) {
        this.bookInfo = bookInfo;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public AbstractOutput getOutput() {
        return output;
    }

    public void setOutput(AbstractOutput output) {
        this.output = output;
    }
}
