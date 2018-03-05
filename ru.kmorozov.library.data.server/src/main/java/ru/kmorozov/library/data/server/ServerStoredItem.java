package ru.kmorozov.library.data.server;

import ru.kmorozov.gbd.core.config.IStoredItem;
import ru.kmorozov.gbd.core.loader.LocalFSStoredItem;
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

    ServerStoredItem(ServerStorage storage, IPage page, String imgFormat) {
        this.storage = storage;
        this.page = page;

        localItem = new LocalFSStoredItem(TempLocalStorage.DEFAULT_TEMP_STORAGE, page, imgFormat);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return localItem.getOutputStream();
    }

    @Override
    public boolean exists() throws IOException {
        return storage.isPageExists(page);
    }

    @Override
    public void delete() throws IOException {
        if (remoteItem != null)
            storage.deleteItem(remoteItem);

        if (localItem.exists())
            localItem.delete();
    }

    @Override
    public void close() throws IOException {
        remoteItem = storage.saveItem(this);
    }

    @Override
    public void write(byte[] bytes, int read) throws IOException {
        localItem.write(bytes, read);
    }

    @Override
    public File asFile() {
        return localItem.asFile();
    }

    public IPage getPage() {
        return this.page;
    }
}
