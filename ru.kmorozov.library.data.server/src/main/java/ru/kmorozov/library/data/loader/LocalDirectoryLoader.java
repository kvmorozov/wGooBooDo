package ru.kmorozov.library.data.loader;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

    private Path basePath;
    private Win32ShellFolderManager2 shellFolderManager;

    public LocalDirectoryLoader(@Autowired String localBasePath) {
        this.basePath = Paths.get(localBasePath);
        this.shellFolderManager = new Win32ShellFolderManager2();
    }

    @Override
    public void load() throws IOException {
        if (loaderState == State.STARTED)
            throw new IllegalStateException("Loader is already started!");

        loaderState = State.STARTED;

        Files.walk(basePath).forEach(filePath -> {
            ServerItem serverItem = new ServerItem(filePath);
            if (serverItem.isDirectory()) {
                Category category = getCategoryByServerItem(serverItem);
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
    public void processLinks() throws IOException {
        links.forEach(linkItem -> {
            try {
                ShellFolder folder = shellFolderManager.createShellFolder(((Path) linkItem).toFile());
                if (folder.isLink()) {
                    ShellFolder link = folder.getLinkLocation();
                    if (!link.exists()) {
                        link = repairLink(link);
                    }

                    Storage parentStorage = storageRepository.findByUrl(folder.getParent());
                    Storage linkStorage = storageRepository.findByUrl(link.toString());
                    if (linkStorage == null) {
                        link = repairLink(link);
                        linkStorage = storageRepository.findByUrl(link.toString());
                        if (linkStorage == null) {
                            logger.log(Level.ERROR, "Invalid link: " + link.toString());
                        }
                    }

                    if (linkStorage != null && parentStorage != null) {
                        Category linkCategory = linkStorage.getMainCategory();
                        linkCategory.addParent(parentStorage.getMainCategory());
                        categoryRepository.save(linkCategory);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public boolean postponedLinksLoad() {
        return false;
    }

    private ShellFolder repairLink(ShellFolder link) throws FileNotFoundException {
        if (link.toString().startsWith("F:\\_Книги"))
            return shellFolderManager.createShellFolder(new File(link.toString().replace("F:\\_Книги", "J:\\_Книги")));
        else
            return link;
    }

    @Override
    protected Stream<ServerItem> getItemsStreamByStorage(Storage storage) throws IOException {
        return Files.walk(Paths.get(storage.getUrl()), 1).map(ServerItem::new);
    }
}