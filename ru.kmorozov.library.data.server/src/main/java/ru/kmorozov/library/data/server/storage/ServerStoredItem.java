package ru.kmorozov.library.data.server.storage;

import ru.kmorozov.gbd.core.config.IStoredItem;
import ru.kmorozov.gbd.core.loader.LocalFSStoredItem;
import ru.kmorozov.gbd.core.loader.TempLocalItem;
import ru.kmorozov.gbd.core.loader.TempLocalStorage;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.onedrive.client.OneDriveItem;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class ServerStoredItem implements IStoredItem {

    private final ServerStorage storage;
    private final IPage page;

    private final LocalFSStoredItem localItem;
    private OneDriveItem remoteItem;

    ServerStoredItem(final ServerStorage storage, final IPage page, final String imgFormat) {
        this.storage = storage;
        this.page = page;

        this.localItem = new TempLocalItem(TempLocalStorage.DEFAULT_TEMP_STORAGE, page, imgFormat);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return this.localItem.getOutputStream();
    }

    @Override
    public boolean exists() throws IOException {
        return this.storage.isPageExists(this.page);
    }

    @Override
    public void delete() throws IOException {
        if (this.remoteItem != null)
            this.storage.deleteItem(this.remoteItem);

        if (this.localItem.exists())
            this.localItem.delete();
    }

    @Override
    public void close() throws IOException {
        this.remoteItem = this.storage.saveItem(this);
    }

    @Override
    public void write(final byte[] bytes, final int read) throws IOException {
        this.localItem.write(bytes, read);
    }

    @Override
    public File asFile() {
        return this.localItem.asFile();
    }

    public IPage getPage() {
        return page;
    }
}
