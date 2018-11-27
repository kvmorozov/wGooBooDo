package ru.kmorozov.library.data.loader.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.library.data.loader.SocketReporter;
import ru.kmorozov.library.data.loader.utils.ConsistencyUtils;
import ru.kmorozov.library.data.loader.utils.WindowsShortcut;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.Category;
import ru.kmorozov.library.data.model.book.LinkInfo;
import ru.kmorozov.library.data.model.book.Storage;
import ru.kmorozov.library.data.model.dto.ItemDTO;
import ru.kmorozov.library.data.server.condition.StorageEnabledCondition;
import ru.kmorozov.onedrive.TaskQueue;
import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.client.OneDriveProvider;
import ru.kmorozov.onedrive.client.walker.OneDriveWalkers;
import ru.kmorozov.onedrive.filesystem.FileSystemProvider;
import ru.kmorozov.onedrive.tasks.DownloadTask;
import ru.kmorozov.onedrive.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */

@Component
@Conditional(StorageEnabledCondition.class)
public class OneDriveLoader extends StoredLoader {

    private static final Logger logger = Logger.getLogger(OneDriveLoader.class);
    private static final String delimiter = Pattern.quote(File.separator);
    private static final String DEFAULT_PARENT = "E:\\tmp";

    @Autowired @Lazy
    private OneDriveProvider api;

    @Override
    public void load() throws IOException {
        this.load(x -> false);
    }

    public void load(Predicate<OneDriveItem> skipCondition) throws IOException {
        this.setState(LoaderExecutor.State.STARTED);

        OneDriveLoader.logger.info("Sync db started.");

        OneDriveWalkers.walk(this.api, skipCondition).forEach(oneDriveItem -> {
            if (this.isStopped())
                OneDriveWalkers.stopAll();

            if (oneDriveItem.isDirectory() && !this.isStopped()) {
                Category category = this.getCategoryByServerItem(new ServerItem(oneDriveItem));
                for (Storage storage : category.getStorages())
                    try {
                        this.updateStorage(storage);
                    } catch (IOException e) {
                        OneDriveLoader.logger.error("Error when updating storage: " + e.getMessage());
                    }
            }
        });

        OneDriveLoader.logger.info("Sync db completed.");

        this.setState(LoaderExecutor.State.STOPPED);
    }

    @Override
    protected Stream<ServerItem> getItemsStreamByStorage(Storage storage) throws IOException {
        OneDriveItem[] children = this.api.getChildren(storage.getUrl());
        return Arrays.stream(children).map(ServerItem::new);
    }

    @Override
    public void processLinks() {
        Stream<Book> lnkBooks = this.booksRepository.streamByBookInfoFormat("LNK");

        lnkBooks.forEach(this::resolveLink);
    }

    @Override
    public void resolveLink(Book lnkBook) {
        if (null != lnkBook.getLinkInfo() || !lnkBook.isLink())
            return;

        OneDriveLoader.logger.info("Resolving link: " + lnkBook.getBookInfo().getFileName());

        try {
            OneDriveItem linkItem = this.api.getItem(lnkBook.getBookInfo().getPath());
            LinkInfo linkInfo = new LinkInfo();

            File tmpFile = File.createTempFile("one", ".lnk");
            this.api.download(linkItem, tmpFile, progressListener -> {
            });
            if (WindowsShortcut.isPotentialValidLink(tmpFile))
                try {
                    WindowsShortcut lnkFile = new WindowsShortcut(tmpFile, Charset.forName("Windows-1251"));
                    if (lnkFile.isDirectory()) {
                        Storage linkedStorage = this.getStorageByLink(lnkFile.getRealFilename());
                        if (null == linkedStorage) {
                            linkInfo.setBroken(true);
                            OneDriveLoader.logger.warn("Storage lnk not found for " + lnkFile.getRealFilename());
                        } else {
                            Storage thisStorage = lnkBook.getStorage();
                            linkInfo.setLinkedStorage(linkedStorage);

                            if (null != thisStorage) {
                                Category linkCategory = linkedStorage.getMainCategory();
                                linkCategory.addParent(thisStorage.getMainCategory());
                                this.categoryRepository.save(linkCategory);
                            }
                        }
                    } else {
                        String realPath = lnkFile.getRealFilename();
                        Book linkedBook = this.getBookByLink(realPath);
                        if (null == linkedBook) {
                            linkInfo.setBroken(true);
                            OneDriveLoader.logger.warn("File lnk not found for " + realPath);
                        } else
                            linkInfo.setLinkedBook(linkedBook);
                    }
                } catch (ParseException e) {
                    OneDriveLoader.logger.error(e);
                }

            tmpFile.delete();

            if (null != linkInfo.getLinkedBook() || null != linkInfo.getLinkedStorage() || linkInfo.isBroken()) {
                lnkBook.setLinkInfo(linkInfo);
                this.booksRepository.save(lnkBook);
            }
        } catch (IOException ioe) {
            OneDriveLoader.logger.error(ioe);
        }
    }

    private Book getBookByLink(String lnkFileName) {
        String[] names = lnkFileName.split(OneDriveLoader.delimiter);
        List<Book> books = this.booksRepository.findAllByBookInfoFileName(names[names.length - 1]);

        return 1 == books.size() ? books.get(0) : null;
    }

    @Override
    public boolean postponedLinksLoad() {
        return true;
    }

    private Storage getStorageByLink(String lnkFileName) {
        String[] names = lnkFileName.split(OneDriveLoader.delimiter);
        List<Storage> storages = this.storageRepository.findAllByName(names[names.length - 1]);
        String parentName;

        for (int index = names.length - 1; 0 < index; index--) {
            if (1 == storages.size())
                break;
            else {
                parentName = names[index - 1];
                if (null != parentName) {
                    List<Storage> filteredStorages = new ArrayList<>();
                    for (Storage storage : storages)
                        if (storage.getParent().getName().equals(parentName))
                            filteredStorages.add(storage);

                    storages = filteredStorages;
                }
            }
        }

        return 1 == storages.size() ? storages.get(0) : null;
    }

    @Override
    public Storage refresh(Storage storage) {
        if ((long) ItemDTO.REFRESH_INTERVAL > System.currentTimeMillis() - storage.getStorageInfo().getLastChecked())
            return storage;

        try {
            OneDriveItem item = this.api.getItem(storage.getUrl());
            ServerItem serverItem = new ServerItem(item);

            StoredLoader.fillStorage(storage, serverItem);

            Map<String, Book> books = ConsistencyUtils.deduplicate(this.booksRepository.findAllByStorage(storage), this.booksRepository)
                    .stream().collect(Collectors.toMap(book -> book.getBookInfo().getPath(), book -> book));
            Map<String, OneDriveItem> children = Arrays.asList(this.api.getChildren(item)).stream()
                    .collect(Collectors.toMap(OneDriveItem::getId, child -> child));

            this.storageRepository.save(storage);
        } catch (IOException e) {
            OneDriveLoader.logger.error(e);
        }
        return storage;
    }

    @Override
    public void downloadBook(Book book) {
        try {
            OneDriveItem bookItem = this.api.getItem(book.getBookInfo().getPath());
            File parent = new File(OneDriveLoader.DEFAULT_PARENT);

            book.getStorage().setLocalPath(OneDriveLoader.DEFAULT_PARENT);
            this.storageRepository.save(book.getStorage());

            int itemPartSize = 0L < bookItem.getSize() ? (int) bookItem.getSize() / 5 : Integer.MAX_VALUE;

            Runnable task = new DownloadTask(
                    new Task.TaskOptions(new TaskQueue(), this.api, FileSystemProvider.FACTORY.readWriteProvider(), new SocketReporter()),
                    parent, bookItem, true, itemPartSize);

            task.run();
        } catch (IOException e) {
            OneDriveLoader.logger.error(e);
        }
    }
}
