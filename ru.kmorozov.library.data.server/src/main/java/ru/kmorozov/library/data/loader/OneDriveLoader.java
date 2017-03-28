package ru.kmorozov.library.data.loader;

import com.wouterbreukink.onedrive.client.OneDriveItem;
import com.wouterbreukink.onedrive.client.OneDriveProvider;
import com.wouterbreukink.onedrive.client.walker.OneDriveWalkers;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kmorozov.library.data.model.book.Category;
import ru.kmorozov.library.data.model.book.Storage;
import sun.awt.shell.ShellFolder;
import sun.awt.shell.Win32ShellFolderManager2;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */

@Component
public class OneDriveLoader extends BaseLoader {

    private static final Logger logger = Logger.getLogger(OneDriveLoader.class);
    @Autowired
    private OneDriveProvider api;

    @Override
    public void load() throws IOException {
        OneDriveWalkers.walk(api).forEach(oneDriveItem -> {
            if (oneDriveItem.isDirectory()) {
                Category category = getCategoryByServerItem(new ServerItem(oneDriveItem));
                for (Storage storage : category.getStorages())
                    try {
                        updateStorage(storage);
                    } catch (IOException e) {
                        logger.log(Level.ERROR, "Error when updating storage: " + e.getMessage());
                    }
            }
        });
    }

    @Override
    protected Stream<ServerItem> getItemsStreamByStorage(Storage storage) throws IOException {
        OneDriveItem[] children = api.getChildren(storage.getUrl());
        return Arrays.asList(children).stream().map(oneDriveItem -> new ServerItem(oneDriveItem));
    }

    @Override
    public void processLinks() throws IOException {
        Win32ShellFolderManager2 shellFolderManager = new Win32ShellFolderManager2();

        links.forEach(linkItem -> {
            try {
                File tmpFile = File.createTempFile("one", "tmplnk");
                api.download((OneDriveItem) linkItem, tmpFile, downloader -> {
                });
                ShellFolder lnkFile = shellFolderManager.createShellFolder(tmpFile);
                if (lnkFile.isLink()) {

                }

                tmpFile.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public boolean postponedLinksLoad() {
        return true;
    }
}