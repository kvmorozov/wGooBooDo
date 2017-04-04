package ru.kmorozov.library.data.loader;

import com.wouterbreukink.onedrive.client.OneDriveItem;
import com.wouterbreukink.onedrive.client.OneDriveProvider;
import com.wouterbreukink.onedrive.client.walker.OneDriveWalkers;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kmorozov.library.data.loader.utils.WindowsShortcut;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.Category;
import ru.kmorozov.library.data.model.book.Storage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */

@Component
public class OneDriveLoader extends BaseLoader {

    private static final Logger logger = Logger.getLogger(OneDriveLoader.class);
    private static final String delimiter = Pattern.quote(File.separator);

    @Autowired
    private OneDriveProvider api;

    @Override
    public void load() throws IOException {
        if (loaderState == State.STARTED)
            throw new IllegalStateException("Loader is already started!");

        loaderState = State.STARTED;

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

        loaderState = State.STOPPED;
    }

    @Override
    protected Stream<ServerItem> getItemsStreamByStorage(Storage storage) throws IOException {
        OneDriveItem[] children = api.getChildren(storage.getUrl());
        return Arrays.asList(children).stream().map(ServerItem::new);
    }

    @Override
    public void processLinks() throws IOException {
        Stream<Book> lnkBooks = booksRepository.streamByBookInfoFormat("LNK");

        lnkBooks.forEach(lnk -> {
            try {
                OneDriveItem linkItem = api.getItem(lnk.getBookInfo().getPath());

                File tmpFile = File.createTempFile("one", ".lnk");
                api.download(linkItem, tmpFile, progressListener -> {
                });
                if (WindowsShortcut.isPotentialValidLink(tmpFile))
                    try {
                        WindowsShortcut lnkFile = new WindowsShortcut(tmpFile, Charset.forName("Windows-1251"));
                        if (lnkFile.isDirectory()) {
                            Storage linkedStorage = getStorageByLink(lnkFile.getRealFilename());
                            Storage thisStorage = lnk.getStorage();

                            if (linkedStorage != null && thisStorage != null) {
                                Category linkCategory = linkedStorage.getMainCategory();
                                linkCategory.addParent(thisStorage.getMainCategory());
                                categoryRepository.save(linkCategory);
                            }
                        } else {
                            String realPath = lnkFile.getRealFilename();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
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

    private Storage getStorageByLink(String lnkFileName) {
        String[] names = lnkFileName.split(delimiter);
        List<Storage> storages = storageRepository.findAllByName(names[names.length - 1]);
        String parentName = null;

        for (int index = names.length - 1; index > 0; index--) {
            if (storages.size() == 1)
                break;
            else {
                parentName = names[index - 1];
                if (parentName != null) {
                    List<Storage> filteredStorages = new ArrayList<>();
                    for (Storage storage : storages)
                        if (storage.getParent().getName().equals(parentName))
                            filteredStorages.add(storage);

                    storages = filteredStorages;
                }
            }
        }

        assert storages.size() == 1 : lnkFileName;
        return storages.get(0);
    }
}
