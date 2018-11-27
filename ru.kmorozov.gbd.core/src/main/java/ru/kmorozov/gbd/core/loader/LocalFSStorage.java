package ru.kmorozov.gbd.core.loader;

import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.gbd.core.config.IIndex;
import ru.kmorozov.gbd.core.config.IStorage;
import ru.kmorozov.gbd.core.config.IStoredItem;
import ru.kmorozov.gbd.core.logic.library.LibraryFactory;
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class LocalFSStorage implements IStorage {

    protected final File storageDir;

    public LocalFSStorage(String storageDirName) {
        storageDir = new File(storageDirName);
    }

    @Override
    public boolean isValidOrCreate() {
        return storageDir.exists() ? storageDir.isDirectory() : storageDir.mkdir();
    }

    @Override
    public IStorage getChildStorage(IBookData bookData) {
        try {
            final Optional<Path> optPath = Files.find(storageDir.toPath(), 1,
                    (path, basicFileAttributes) -> path.toString().contains(bookData.getVolumeId())).findAny();
            if (optPath.isPresent()) return new LocalFSStorage(optPath.get().toString());
        } catch (final IOException ignored) {
        }

        final String directoryName = storageDir.getPath() + '\\' + bookData.getTitle()
                .replace(":", "")
                .replace("<", "")
                .replace(">", "")
                .replace("?", "")
                .replace("/", ".");
        final String volumeId = bookData.getVolumeId();
        return new LocalFSStorage(StringUtils.isEmpty(volumeId) ? directoryName : directoryName + ' ' + bookData.getVolumeId());
    }

    @Override
    public int size() {
        return storageDir.listFiles() == null ? 0 : storageDir.listFiles().length;
    }

    @Override
    public Set<String> getBookIdsList() throws IOException {
        final Set<String> bookIdsList = new HashSet<>();

        Files.walk(Paths.get(storageDir.toURI())).forEach(filePath -> {
            if (filePath.toFile().isDirectory()) {
                final String[] nameParts = filePath.toFile().getName().split(" ");
                if (LibraryFactory.isValidId(nameParts[nameParts.length - 1]))
                    bookIdsList.add(nameParts[nameParts.length - 1]);
            }
        });

        return bookIdsList;
    }

    @Override
    public boolean isPageExists(IPage page) throws IOException {
        return 0L == Files.find(storageDir.toPath(), 1,
                (path, basicFileAttributes) -> path.toString().contains(page.getOrder() + '_' + page.getPid() + '.'),
                FileVisitOption.FOLLOW_LINKS).count();
    }

    @Override
    public Stream<Path> getFiles() throws IOException {
        return Files.walk(storageDir.toPath());
    }

    @Override
    public IStoredItem getStoredItem(IPage page, String imgFormat) throws IOException {
        return new LocalFSStoredItem(this, page, imgFormat);
    }

    @Override
    public void refresh() {

    }

    public File getStorageDir() {
        return this.storageDir;
    }

    @Override
    public IIndex getIndex(String indexName, boolean createIfNotExists) {
        return new LocalFSIndex(this, indexName, createIfNotExists);
    }
}
