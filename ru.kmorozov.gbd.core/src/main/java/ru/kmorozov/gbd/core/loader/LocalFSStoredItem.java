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

    public LocalFSStoredItem(final LocalFSStorage storage, final IPage page, final String imgFormat) {
        this.storage = storage;
        this.page = page;
        this.imgFormat = imgFormat;

        this.init();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return this.outputStream = this.outputStream == null ? new FileOutputStream(this.outputFile) : this.outputStream;
    }

    @Override
    public boolean exists() {
        return this.outputFile.exists();
    }

    @Override
    public void delete() {
        this.outputFile.delete();
    }

    @Override
    public void close() throws IOException {
        if (this.outputStream != null)
            this.outputStream.close();
    }

    @Override
    public void write(final byte[] bytes, final int read) throws IOException {
        this.getOutputStream().write(bytes, 0, read);
    }

    @Override
    public File asFile() {
        return this.outputFile;
    }

    protected void init() {
        this.outputFile = new File(this.storage.getStorageDir().getPath() + '\\' + this.page.getOrder() + '_' + this.page.getPid() + '.' + this.imgFormat);
    }
}
