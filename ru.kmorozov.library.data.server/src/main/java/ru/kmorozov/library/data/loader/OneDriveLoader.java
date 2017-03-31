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
import ru.kmorozov.library.data.repository.BooksRepository;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
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
    @Autowired
    private BooksRepository booksRepository;

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
}
