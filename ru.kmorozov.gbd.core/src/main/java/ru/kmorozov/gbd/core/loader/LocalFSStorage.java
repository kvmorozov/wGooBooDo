package ru.kmorozov.gbd.core.loader;

import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.config.IIndex;
import ru.kmorozov.gbd.core.config.IStorage;
import ru.kmorozov.gbd.core.config.IStoredItem;
import ru.kmorozov.gbd.core.logic.library.LibraryFactory;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData;
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.gbd.utils.Images;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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

import static ru.kmorozov.gbd.core.config.constants.GoogleConstants.DEFAULT_PAGE_WIDTH;

public class LocalFSStorage implements IStorage {

    protected final File storageDir;
    protected final Logger logger;

    public LocalFSStorage(String storageDirName) {
        storageDir = new File(storageDirName);

        logger = Logger.getLogger(LocalFSStorage.class);
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
    public Stream<IStoredItem> getItems() throws IOException {
        return Files.walk(storageDir.toPath()).map(RawFileItem::new);
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

    @Override
    public void restoreState(IBookInfo bookInfo) throws IOException {
        Stream<IStoredItem> items = getItems();
        if (items == null)
            return;

        final int imgWidth = 0 == GBDOptions.getImageWidth() ? DEFAULT_PAGE_WIDTH : GBDOptions.getImageWidth();

        items.forEach(item -> {
            Path filePath = item.asFile().toPath();

            if (Images.isImageFile(filePath)) {
                final String fileName = filePath.getFileName().toString();
                final String[] nameParts = fileName.split("\\.")[0].split("_");
                final AbstractPage _page = (AbstractPage) bookInfo.getPages().getPageByPid(nameParts[1]);
                final int order = Integer.valueOf(nameParts[0]);
                if (null == _page) {
                    logger.severe(String.format("Page %s not found!", fileName));
                    try {
                        item.delete();
                        logger.severe(String.format("Page %s deleted!", fileName));
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        if (GBDOptions.reloadImages()) {
                            final BufferedImage bimg = ImageIO.read(item.asFile());
                            _page.setDataProcessed(bimg.getWidth() >= imgWidth);

                            // 1.4 - эмпирически, высота переменная
                            if (bimg.getWidth() * 1.4 > bimg.getHeight()) {
                                item.delete();
                                _page.setDataProcessed(false);
                                logger.severe(String.format("Page %s deleted!", _page.getPid()));
                            }
                        } else _page.setDataProcessed(true);

                        if (Images.isInvalidImage(filePath, imgWidth)) {
                            _page.setDataProcessed(false);
                            item.delete();
                            logger.severe(String.format("Page %s deleted!", _page.getPid()));
                        } else if (_page.getOrder() != order && !_page.isGapPage()) {
                            final File oldFile = item.asFile();
                            final File newFile = new File(filePath.toString().replace(order + "_", _page.getOrder() + "_"));
                            if (!newFile.exists())
                                oldFile.renameTo(newFile);
                            _page.setDataProcessed(true);
                            logger.severe(String.format("Page %s renamed!", _page.getPid()));
                        }
                    } catch (final IOException e) {
                        // Значит файл с ошибкой
                        try {
                            item.delete();
                        } catch (IOException e1) {
                            logger.severe(String.format("Cannot delete page %s!", _page.getPid()));
                        }
                        _page.setDataProcessed(false);
                        logger.severe(String.format("Page %s deleted!", _page.getPid()));
                    }

                    _page.setFileExists(true);
                }
            }
        });
    }
}
