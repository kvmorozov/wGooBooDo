package ru.kmorozov.library.data.server;

import org.apache.commons.lang3.StringUtils;
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

    private OneDriveProvider api;

    private OneDriveItem root;

    private OneDriveItem[] children;

    public ServerStorage(OneDriveProvider api, OneDriveItem root) {
        this.api = api;
        this.root = root;
    }

    @Override
    public boolean isValidOrCreate() {
        return root != null && root.isDirectory();
    }

    @Override
    public IStorage getChildStorage(IBookData bookData) throws IOException {
        for (OneDriveItem child : getChildren())
            if (child.getName().contains(bookData.getVolumeId()))
                return new ServerStorage(api, child);

        final String volumeId = bookData.getVolumeId();
        final String normalizedName = bookData.getTitle()
                .replace(":", "")
                .replace("<", "")
                .replace(">", "")
                .replace("?", "")
                .replace("/", ".");
        final String childName = StringUtils.isEmpty(volumeId) ? normalizedName : normalizedName + ' ' + bookData.getVolumeId();

        return new ServerStorage(api, api.createFolder(root, childName));
    }

    @Override
    public int size() throws IOException {
        return api.getChildren(root).length;
    }

    @Override
    public Set<String> getBookIdsList() throws IOException {
        final Set<String> bookIdsList = new HashSet<>();

        for (OneDriveItem child : getChildren())
            if (child.isDirectory()) {
                final String[] nameParts = child.getName().split(" ");
                if (LibraryFactory.isValidId(nameParts[nameParts.length - 1]))
                    bookIdsList.add(nameParts[nameParts.length - 1]);
            }

        return bookIdsList;
    }

    @Override
    public boolean isPageExists(IPage page) throws IOException {
        for (OneDriveItem child : getChildren())
            if (!child.isDirectory())
                if (child.getName().contains("\\" + page.getOrder() + '_' + page.getPid() + '.'))
                    return true;

        return false;
    }

    @Override
    public Stream<Path> getFiles() throws IOException {
        return null;
    }

    @Override
    public IStoredItem getStoredItem(IPage page, String imgFormat) throws IOException {
        return new ServerStoredItem(this, page, imgFormat);
    }

    @Override
    public void refresh() {
        children = null;
    }

    private OneDriveItem[] getChildren() throws IOException {
        if (children == null)
            children = api.getChildren(root);

        return children;
    }

    void deleteItem(OneDriveItem item) throws IOException {
        api.delete(item);
    }

    OneDriveItem saveItem(ServerStoredItem item) throws IOException {
        OneDriveItem result = isPageExists(item.getPage()) ? api.replaceFile(root, item.asFile()) : api.uploadFile(root, item.asFile());

        refresh();

        return result;
    }
}
