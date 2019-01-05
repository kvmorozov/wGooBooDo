package ru.kmorozov.gbd.core.loader

import org.apache.commons.lang3.StringUtils
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.IIndex
import ru.kmorozov.gbd.core.config.IStorage
import ru.kmorozov.gbd.core.config.IStoredItem
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.DEFAULT_PAGE_WIDTH
import ru.kmorozov.gbd.core.logic.library.LibraryFactory
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo
import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.gbd.utils.Images
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.function.BiPredicate
import java.util.stream.Stream
import javax.imageio.ImageIO

open class LocalFSStorage(storageDirName: String) : IStorage {

    val storageDir: File
    protected val logger: Logger

    override val isValidOrCreate: Boolean
        get() = if (storageDir.exists()) storageDir.isDirectory else storageDir.mkdir()

    override val bookIdsList: Set<String>
        @Throws(IOException::class)
        get() {
            val bookIdsList = HashSet<String>()

            Files.walk(Paths.get(storageDir.toURI())).forEach { filePath ->
                if (filePath.toFile().isDirectory) {
                    val nameParts = filePath.toFile().name.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (LibraryFactory.isValidId(nameParts[nameParts.size - 1]))
                        bookIdsList.add(nameParts[nameParts.size - 1])
                }
            }

            return bookIdsList
        }

    override val items: Stream<IStoredItem>
        @Throws(IOException::class)
        get() = Files.walk(storageDir.toPath()).filter { !it.toFile().isDirectory }.map { RawFileItem(it) }

    init {
        storageDir = File(storageDirName)

        logger = Logger.getLogger(LocalFSStorage::class.java, storageDirName)
    }

    override fun getChildStorage(bookData: IBookData): IStorage {
        try {
            val optPath = Files.find(storageDir.toPath(), 1,
                    BiPredicate<Path, BasicFileAttributes> { path, _ -> path.toString().contains(bookData.volumeId) }).findAny()
            if (optPath.isPresent) return LocalFSStorage(optPath.get().toString())
        } catch (ignored: IOException) {
        }

        val directoryName = storageDir.path + '\\'.toString() + bookData.title
                .replace(":", "")
                .replace("<", "")
                .replace(">", "")
                .replace("?", "")
                .replace("/", ".")
        val volumeId = bookData.volumeId
        return LocalFSStorage(if (StringUtils.isEmpty(volumeId)) directoryName else directoryName + ' '.toString() + bookData.volumeId)
    }

    override fun size(): Int {
        return if (storageDir.listFiles() == null) 0 else storageDir.listFiles().size
    }

    @Throws(IOException::class)
    override fun isPageExists(page: IPage): Boolean {
        val files = File(storageDir.toPath().toUri()).list { _, fileName -> fileName.contains(page.order.toString() + '_'.toString() + page.pid + ".") }
        return files.size > 0
    }

    @Throws(IOException::class)
    override fun getStoredItem(page: IPage, imgFormat: String): IStoredItem {
        return LocalFSStoredItem(this, page, imgFormat)
    }

    override fun refresh() {

    }

    override fun getIndex(indexName: String, createIfNotExists: Boolean): IIndex {
        return LocalFSIndex(this, indexName, createIfNotExists)
    }

    @Throws(IOException::class)
    override fun restoreState(bookInfo: IBookInfo) {
        val items = items

        val imgWidth = if (0 == GBDOptions.imageWidth) DEFAULT_PAGE_WIDTH else GBDOptions.imageWidth

        items.forEach { item ->
            val filePath = item.asFile().toPath()

            if (Images.isImageFile(filePath)) {
                val fileName = filePath.fileName.toString()
                val nameParts = fileName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val _page = bookInfo.pages.getPageByPid(nameParts[1]) as AbstractPage?
                if (null == _page) {
                    logger.severe(String.format("Page %s not found!", fileName))
                    try {
                        item.delete()
                        logger.severe(String.format("Page %s deleted!", fileName))
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    try {
                        val order = Integer.valueOf(nameParts[0])

                        if (GBDOptions.reloadImages) {
                            val bimg = ImageIO.read(item.asFile())
                            _page.isDataProcessed = bimg != null && bimg.width >= imgWidth

                            // 1.4 - эмпирически, высота переменная
                            if (bimg == null || (bimg.width * 1.4 > bimg.height && bimg.height < 800)) {
                                item.delete()
                                _page.isDataProcessed = false
                                logger.severe(String.format("Page %s deleted!", _page.pid))
                            }
                        } else
                            _page.isDataProcessed = true

                        if (_page.isDataProcessed)
                            if (Images.isInvalidImage(filePath, imgWidth)) {
                                _page.isDataProcessed = false
                                item.delete()
                                logger.severe(String.format("Page %s deleted!", _page.pid))
                            } else if (_page.order != order && !_page.isGapPage) {
                                val oldFile = item.asFile()
                                val newFile = File(filePath.toString().replace(order.toString() + "_", _page.order.toString() + "_"))
                                if (!newFile.exists()) {
                                    oldFile.renameTo(newFile)
                                    logger.severe(String.format("Page %s renamed!", _page.pid))
                                }
                            }
                    } catch (e: IOException) {
                        // Значит файл с ошибкой
                        try {
                            item.delete()
                        } catch (e1: IOException) {
                            logger.severe(String.format("Cannot delete page %s!", _page.pid))
                        }

                        _page.isDataProcessed = false
                        logger.severe(String.format("Page %s deleted!", _page.pid))
                    }

                    _page.isFileExists = item.asFile().exists()
                }
            }
        }
    }
}
