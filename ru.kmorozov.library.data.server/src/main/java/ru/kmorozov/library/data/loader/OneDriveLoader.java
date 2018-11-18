package ru.kmorozov.library.data.loader;

import ru.kmorozov.onedrive.TaskQueue;
import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.client.OneDriveProvider;
import ru.kmorozov.onedrive.client.walker.OneDriveWalkers;
import ru.kmorozov.onedrive.filesystem.FileSystemProvider.FACTORY;
import ru.kmorozov.onedrive.tasks.DownloadTask;
import ru.kmorozov.onedrive.tasks.Task.TaskOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.library.data.loader.LoaderExecutor.State;
import ru.kmorozov.library.data.loader.utils.ConsistencyUtils;
import ru.kmorozov.library.data.loader.utils.WindowsShortcut;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.Category;
import ru.kmorozov.library.data.model.book.LinkInfo;
import ru.kmorozov.library.data.model.book.Storage;
import ru.kmorozov.library.data.model.dto.ItemDTO;

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
public class OneDriveLoader extends BaseLoader {

    private static final Logger logger = Logger.getLogger(OneDriveLoader.class);
    private static final String delimiter = Pattern.quote(File.separator);
    private static final String DEFAULT_PARENT = "E:\\tmp";

    @Autowired
    private OneDriveProvider api;

    @Override
    public void load() throws IOException {
        load(x -> false);
    }

    public void load(final Predicate<OneDriveItem> skipCondition) throws IOException {
        setState(State.STARTED);

        logger.info("Sync db started.");

        OneDriveWalkers.walk(api, skipCondition).forEach(oneDriveItem -> {
            if (isStopped())
                OneDriveWalkers.stopAll();

            if (oneDriveItem.isDirectory() && !isStopped()) {
                final Category category = getCategoryByServerItem(new ServerItem(oneDriveItem));
                for (final Storage storage : category.getStorages())
                    try {
                        updateStorage(storage);
                    } catch (final IOException e) {
                        logger.error("Error when updating storage: " + e.getMessage());
                    }
            }
        });

        logger.info("Sync db completed.");

        setState(State.STOPPED);
    }

    @Override
    protected Stream<ServerItem> getItemsStreamByStorage(final Storage storage) throws IOException {
        final OneDriveItem[] children = api.getChildren(storage.getUrl());
        return Arrays.stream(children).map(ServerItem::new);
    }

    @Override
    public void processLinks() {
        final Stream<Book> lnkBooks = booksRepository.streamByBookInfoFormat("LNK");

        lnkBooks.forEach(this::resolveLink);
    }

    @Override
    public void resolveLink(final Book lnkBook) {
        if (null != lnkBook.getLinkInfo() || !lnkBook.isLink())
            return;

        logger.info("Resolving link: " + lnkBook.getBookInfo().getFileName());

        try {
            final OneDriveItem linkItem = api.getItem(lnkBook.getBookInfo().getPath());
            final LinkInfo linkInfo = new LinkInfo();

            final File tmpFile = File.createTempFile("one", ".lnk");
            api.download(linkItem, tmpFile, progressListener -> {
            });
            if (WindowsShortcut.isPotentialValidLink(tmpFile))
                try {
                    final WindowsShortcut lnkFile = new WindowsShortcut(tmpFile, Charset.forName("Windows-1251"));
                    if (lnkFile.isDirectory()) {
                        final Storage linkedStorage = getStorageByLink(lnkFile.getRealFilename());
                        if (null == linkedStorage) {
                            linkInfo.setBroken(true);
                            logger.warn("Storage lnk not found for " + lnkFile.getRealFilename());
                        } else {
                            final Storage thisStorage = lnkBook.getStorage();
                            linkInfo.setLinkedStorage(linkedStorage);

                            if (null != thisStorage) {
                                final Category linkCategory = linkedStorage.getMainCategory();
                                linkCategory.addParent(thisStorage.getMainCategory());
                                categoryRepository.save(linkCategory);
                            }
                        }
                    } else {
                        final String realPath = lnkFile.getRealFilename();
                        final Book linkedBook = getBookByLink(realPath);
                        if (null == linkedBook) {
                            linkInfo.setBroken(true);
                            logger.warn("File lnk not found for " + realPath);
                        } else
                            linkInfo.setLinkedBook(linkedBook);
                    }
                } catch (final ParseException e) {
                    logger.error(e);
                }

            tmpFile.delete();

            if (null != linkInfo.getLinkedBook() || null != linkInfo.getLinkedStorage() || linkInfo.isBroken()) {
                lnkBook.setLinkInfo(linkInfo);
                booksRepository.save(lnkBook);
            }
        } catch (final IOException ioe) {
            logger.error(ioe);
        }
    }

    private Book getBookByLink(final String lnkFileName) {
        final String[] names = lnkFileName.split(delimiter);
        final List<Book> books = booksRepository.findAllByBookInfoFileName(names[names.length - 1]);

        return 1 == books.size() ? books.get(0) : null;
    }

    @Override
    public boolean postponedLinksLoad() {
        return true;
    }

    private Storage getStorageByLink(final String lnkFileName) {
        final String[] names = lnkFileName.split(delimiter);
        List<Storage> storages = storageRepository.findAllByName(names[names.length - 1]);
        String parentName;

        for (int index = names.length - 1; 0 < index; index--) {
            if (1 == storages.size())
                break;
            else {
                parentName = names[index - 1];
                if (null != parentName) {
                    final List<Storage> filteredStorages = new ArrayList<>();
                    for (final Storage storage : storages)
                        if (storage.getParent().getName().equals(parentName))
                            filteredStorages.add(storage);

                    storages = filteredStorages;
                }
            }
        }

        return 1 == storages.size() ? storages.get(0) : null;
    }

    @Override
    public Storage refresh(final Storage storage) {
        if ((long) ItemDTO.REFRESH_INTERVAL > System.currentTimeMillis() - storage.getStorageInfo().getLastChecked())
            return storage;

        try {
            final OneDriveItem item = api.getItem(storage.getUrl());
            final ServerItem serverItem = new ServerItem(item);

            fillStorage(storage, serverItem);

            final Map<String, Book> books = ConsistencyUtils.deduplicate(booksRepository.findAllByStorage(storage), booksRepository)
                    .stream().collect(Collectors.toMap(book -> book.getBookInfo().getPath(), book -> book));
            final Map<String, OneDriveItem> children = Arrays.asList(api.getChildren(item)).stream()
                    .collect(Collectors.toMap(OneDriveItem::getId, child -> child));

            storageRepository.save(storage);
        } catch (final IOException e) {
            logger.error(e);
        }
        return storage;
    }

    @Override
    public void downloadBook(final Book book) {
        try {
            final OneDriveItem bookItem = api.getItem(book.getBookInfo().getPath());
            final File parent = new File(DEFAULT_PARENT);

            book.getStorage().setLocalPath(DEFAULT_PARENT);
            storageRepository.save(book.getStorage());

            final int itemPartSize = 0L < bookItem.getSize() ? (int) bookItem.getSize() / 5 : Integer.MAX_VALUE;

            final Runnable task = new DownloadTask(
                    new TaskOptions(new TaskQueue(), api, FACTORY.readWriteProvider(), new SocketReporter()),
                    parent, bookItem, true, itemPartSize);

            task.run();
        } catch (final IOException e) {
            logger.error(e);
        }
    }
}
