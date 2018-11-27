package ru.kmorozov.library.data.server.storage;

import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.gbd.core.config.IIndex;
import ru.kmorozov.gbd.core.config.IStorage;
import ru.kmorozov.gbd.core.config.IStoredItem;
import ru.kmorozov.gbd.core.logic.library.LibraryFactory;
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.client.OneDriveProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class ServerStorage implements IStorage {

    private final OneDriveProvider api;

    private final OneDriveItem root;

    private OneDriveItem[] children;

    public ServerStorage(final OneDriveProvider api, final OneDriveItem root) {
        this.api = api;
        this.root = root;
    }

    @Override
    public boolean isValidOrCreate() {
        return this.root != null && this.root.isDirectory();
    }

    @Override
    public IStorage getChildStorage(final IBookData bookData) throws IOException {
        for (final OneDriveItem child : this.getChildren())
            if (child.getName().contains(bookData.getVolumeId()))
                return new ServerStorage(this.api, child);

        String volumeId = bookData.getVolumeId();
        String normalizedName = bookData.getTitle()
                .replace(":", "")
                .replace("<", "")
                .replace(">", "")
                .replace("?", "")
                .replace("/", ".");
        String childName = StringUtils.isEmpty(volumeId) ? normalizedName : normalizedName + ' ' + bookData.getVolumeId();

        return new ServerStorage(this.api, this.api.createFolder(this.root, childName));
    }

    @Override
    public int size() throws IOException {
        return this.api.getChildren(this.root).length;
    }

    @Override
    public Set<String> getBookIdsList() throws IOException {
        Set<String> bookIdsList = new HashSet<>();

        for (final OneDriveItem child : this.getChildren())
            if (child.isDirectory()) {
                String[] nameParts = child.getName().split(" ");
                if (LibraryFactory.isValidId(nameParts[nameParts.length - 1]))
                    bookIdsList.add(nameParts[nameParts.length - 1]);
            }

        return bookIdsList;
    }

    @Override
    public boolean isPageExists(final IPage page) throws IOException {
        for (final OneDriveItem child : this.getChildren())
            if (!child.isDirectory())
                if (child.getName().contains(page.getOrder() + "_" + page.getPid() + "."))
                    return true;

        return false;
    }

    @Override
    public Stream<Path> getFiles() throws IOException {
        return null;
    }

    @Override
    public IStoredItem getStoredItem(final IPage page, final String imgFormat) throws IOException {
        return new ServerStoredItem(this, page, imgFormat);
    }

    @Override
    public void refresh() {
        this.children = null;
    }

    @Override
    public IIndex getIndex(final String indexName, final boolean createIfNotExists) {
        return null;
    }

    private OneDriveItem[] getChildren() throws IOException {
        if (this.children == null)
            this.children = this.api.getChildren(this.root);

        return this.children;
    }

    void deleteItem(final OneDriveItem item) throws IOException {
        this.api.delete(item);
    }

    OneDriveItem saveItem(final ServerStoredItem item) throws IOException {
        final OneDriveItem result = this.isPageExists(item.getPage()) ? this.api.replaceFile(this.root, item.asFile()) : this.api.uploadFile(this.root, item.asFile());

        this.refresh();

        return result;
    }
}
