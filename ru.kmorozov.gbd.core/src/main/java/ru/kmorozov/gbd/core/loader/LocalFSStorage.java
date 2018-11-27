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

    public LocalFSStorage(final String storageDirName) {
        this.storageDir = new File(storageDirName);
    }

    @Override
    public boolean isValidOrCreate() {
        return this.storageDir.exists() ? this.storageDir.isDirectory() : this.storageDir.mkdir();
    }

    @Override
    public IStorage getChildStorage(final IBookData bookData) {
        try {
            Optional<Path> optPath = Files.find(this.storageDir.toPath(), 1,
                    (path, basicFileAttributes) -> path.toString().contains(bookData.getVolumeId())).findAny();
            if (optPath.isPresent()) return new LocalFSStorage(optPath.get().toString());
        } catch (IOException ignored) {
        }

        String directoryName = this.storageDir.getPath() + '\\' + bookData.getTitle()
                .replace(":", "")
                .replace("<", "")
                .replace(">", "")
                .replace("?", "")
                .replace("/", ".");
        String volumeId = bookData.getVolumeId();
        return new LocalFSStorage(StringUtils.isEmpty(volumeId) ? directoryName : directoryName + ' ' + bookData.getVolumeId());
    }

    @Override
    public int size() {
        return this.storageDir.listFiles() == null ? 0 : this.storageDir.listFiles().length;
    }

    @Override
    public Set<String> getBookIdsList() throws IOException {
        Set<String> bookIdsList = new HashSet<>();

        Files.walk(Paths.get(this.storageDir.toURI())).forEach(filePath -> {
            if (filePath.toFile().isDirectory()) {
                String[] nameParts = filePath.toFile().getName().split(" ");
                if (LibraryFactory.isValidId(nameParts[nameParts.length - 1]))
                    bookIdsList.add(nameParts[nameParts.length - 1]);
            }
        });

        return bookIdsList;
    }

    @Override
    public boolean isPageExists(final IPage page) throws IOException {
        return 0L == Files.find(this.storageDir.toPath(), 1,
                (path, basicFileAttributes) -> path.toString().contains(page.getOrder() + '_' + page.getPid() + '.'),
                FileVisitOption.FOLLOW_LINKS).count();
    }

    @Override
    public Stream<Path> getFiles() throws IOException {
        return Files.walk(this.storageDir.toPath());
    }

    @Override
    public IStoredItem getStoredItem(final IPage page, final String imgFormat) throws IOException {
        return new LocalFSStoredItem(this, page, imgFormat);
    }

    @Override
    public void refresh() {

    }

    public File getStorageDir() {
        return storageDir;
    }

    @Override
    public IIndex getIndex(final String indexName, final boolean createIfNotExists) {
        return new LocalFSIndex(this, indexName, createIfNotExists);
    }
}
