package ru.kmorozov.library.data.loader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kmorozov.library.data.loader.LoaderExecutor.State;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.Category;
import ru.kmorozov.library.data.model.book.Storage;
import sun.awt.shell.ShellFolder;
import sun.awt.shell.Win32ShellFolderManager2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Created by km on 26.12.2016.
 */

@Component
public class LocalDirectoryLoader extends BaseLoader {

    private static final Logger logger = Logger.getLogger(LocalDirectoryLoader.class);

    private final Path basePath;
    private final Win32ShellFolderManager2 shellFolderManager;

    public LocalDirectoryLoader(@Autowired final String localBasePath) {
        this.basePath = Paths.get(localBasePath);
        this.shellFolderManager = new Win32ShellFolderManager2();
    }

    @Override
    public void load() throws IOException {
        Files.walk(basePath).forEach(filePath -> {
            final ServerItem serverItem = new ServerItem(filePath);
            if (serverItem.isDirectory()) {
                final Category category = getCategoryByServerItem(serverItem);
                for (final Storage storage : category.getStorages())
                    try {
                        updateStorage(storage);
                    } catch (final IOException e) {
                        logger.log(Level.ERROR, "Error when updating storage: " + e.getMessage());
                    }
            }
        });

        setState(State.STOPPED);
    }

    @Override
    public void processLinks() {
        links.forEach(linkItem -> {
            try {
                final ShellFolder folder = shellFolderManager.createShellFolder(((Path) linkItem).toFile());
                if (folder.isLink()) {
                    ShellFolder link = folder.getLinkLocation();
                    if (!link.exists()) {
                        link = repairLink(link);
                    }

                    final Storage parentStorage = storageRepository.findByUrl(folder.getParent());
                    Storage linkStorage = storageRepository.findByUrl(link.toString());
                    if (null == linkStorage) {
                        link = repairLink(link);
                        linkStorage = storageRepository.findByUrl(link.toString());
                        if (null == linkStorage) {
                            logger.log(Level.ERROR, "Invalid link: " + link);
                        }
                    }

                    if (null != linkStorage && null != parentStorage) {
                        final Category linkCategory = linkStorage.getMainCategory();
                        linkCategory.addParent(parentStorage.getMainCategory());
                        categoryRepository.save(linkCategory);
                    }
                }
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public boolean postponedLinksLoad() {
        return false;
    }

    private ShellFolder repairLink(final ShellFolder link) throws FileNotFoundException {
        if (link.toString().startsWith("F:\\_Книги"))
            return shellFolderManager.createShellFolder(new File(link.toString().replace("F:\\_Книги", "J:\\_Книги")));
        else
            return link;
    }

    @Override
    protected Stream<ServerItem> getItemsStreamByStorage(final Storage storage) throws IOException {
        return Files.walk(Paths.get(storage.getUrl()), 1).map(ServerItem::new);
    }

    @Override
    public Storage refresh(final Storage storage) {
        return storage;
    }

    @Override
    public void resolveLink(final Book lnkBook) {

    }

    @Override
    public void downloadBook(final Book book) {

    }
}