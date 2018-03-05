package ru.kmorozov.gbd.core.loader;

import ru.kmorozov.gbd.core.config.IStoredItem;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LocalFSStoredItem implements IStoredItem {

    protected File outputFile;
    protected final LocalFSStorage storage;
    protected final IPage page;
    protected final String imgFormat;

    private OutputStream outputStream;

    public LocalFSStoredItem(LocalFSStorage storage, IPage page, String imgFormat) {
        this.storage = storage;
        this.page = page;
        this.imgFormat = imgFormat;

        init();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return outputStream = new FileOutputStream(outputFile);
    }

    @Override
    public boolean exists() {
        return outputFile.exists();
    }

    @Override
    public void delete() {
        outputFile.delete();
    }

    @Override
    public void close() throws IOException {
        if (outputStream != null)
            outputStream.close();
    }

    @Override
    public void write(byte[] bytes, int read) throws IOException {
        if (outputStream != null)
            outputStream.write(bytes, 0, read);
    }

    @Override
    public File asFile() {
        return outputFile;
    }

    protected void init() {
        outputFile = new File(storage.getStorageDir().getPath() + '\\' + page.getOrder() + '_' + page.getPid() + '.' + imgFormat);
    }
}
