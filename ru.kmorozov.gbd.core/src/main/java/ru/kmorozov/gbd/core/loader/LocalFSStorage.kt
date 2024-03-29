package ru.kmorozov.gbd.core.loader

import com.google.common.base.Strings
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.IIndex
import ru.kmorozov.gbd.core.config.IStorage
import ru.kmorozov.gbd.core.config.IStoredItem
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.DEFAULT_PAGE_WIDTH
import ru.kmorozov.gbd.core.logic.library.LibraryFactory
import ru.kmorozov.gbd.core.logic.model.book.base.*
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.gbd.utils.Images
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors
import javax.imageio.ImageIO

open class LocalFSStorage internal constructor(storageDirName: String) : IStorage {

    val storageDir: File
    protected val logger: Logger
    private var detectedItems: MutableSet<MayBePageItem> = HashSet()

    private val indexes: MutableMap<String, IIndex> = mapOf<String, LocalFSIndex>().toMutableMap()

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

    override val items: Set<IStoredItem>
        @Throws(IOException::class)
        get() = getOrFindItems()

    private fun getOrFindItems(): Set<IStoredItem> {
        if (detectedItems.isEmpty()) {
            detectedItems = Files.walk(storageDir.toPath())
                    .filter { !it.toFile().isDirectory && Images.isImageFile(it) }
                    .map { MayBePageItem(it.toFile()) }
                    .sorted { o1, o2 -> o1.pageNum.compareTo(o2.pageNum) }
                    .collect(Collectors.toSet())
        }

        return detectedItems
    }

    override fun getChildStorage(bookData: IBookData): IStorage {
        try {
            val optPath = Files.find(storageDir.toPath(), 1,
                { path, _ -> path.toString().contains(bookData.volumeId) }).findAny()
            if (optPath.isPresent) return getStorage(optPath.get().toString())
        } catch (ignored: IOException) {
        }

        val directoryName = storageDir.path + File.separator + bookData.title
                .replace(":", "")
                .replace("<", "")
                .replace(">", "")
                .replace("?", "")
                .replace("\"", "")
                .replace("/", ".")
        return getStorage(if (Strings.isNullOrEmpty(bookData.volumeId) || bookData.volumeId.equals(bookData.title))
            directoryName else directoryName + ' '.toString() + bookData.volumeId)
    }

    override fun size(): Int {
        return if (storageDir.listFiles() == null) 0 else storageDir.listFiles()!!.size
    }

    @Throws(IOException::class)
    override fun isPageExists(page: IPage): Boolean {
        val files = File(storageDir.toPath().toUri()).list { _, fileName -> fileName.contains(page.order.toString() + '_'.toString() + page.pid + ".") }
        return files!!.isNotEmpty()
    }

    @Throws(IOException::class)
    override fun getStoredItem(page: IPage, imgFormat: String): IStoredItem {
        return MayBePageItem(File(storageDir.path + File.separator + page.order + '_'.toString() + page.pid + '.'.toString() + imgFormat), page)
    }

    override fun refresh() {

    }

    override fun storeItem(item: IStoredItem) {
        if (item is MayBePageItem) {
            detectedItems.add(item)
        }

//        item.flush()
    }

    override fun getIndex(indexName: String, createIfNotExists: Boolean): IIndex {
        if (!indexes.containsKey(indexName)) {
            val indexFile = File(storageDir.path + File.separator + indexName)
            if (indexFile.exists())
                indexes[indexName] = FileBasedIndex(this, indexFile)
            else if (!indexFile.exists() && createIfNotExists) {
                indexFile.createNewFile()
                indexes[indexName] = FileBasedIndex(this, indexFile)
            } else
                indexes[indexName] = LocalFSIndex(this)
        }
        return indexes.get(indexName)!!
    }

    @Throws(IOException::class)
    override fun restoreState(bookInfo: IBookInfo) {
        val imgWidth = if (0 == GBDOptions.imageWidth) DEFAULT_PAGE_WIDTH else GBDOptions.imageWidth

        items.forEach { item ->
            val filePath = item.asFile().toPath()

            if (item is MayBePageItem) {
                val fileName = filePath.fileName.toString()
                val nameParts = fileName.split("[_.]".toRegex())

                try {
                    val page = bookInfo.pages.getPageByPid(nameParts[1]) as AbstractPage

                    item.upgrade(page)

                    try {
                        val order = Integer.valueOf(nameParts[0])

                        if (GBDOptions.reloadImages) {
                            val bimg = ImageIO.read(item.asFile())
                            page.isDataProcessed = bimg != null && bimg.width >= imgWidth

                            // 1.4 - эмпирически, высота переменная
                            if (bimg == null || (bimg.width * 1.4 > bimg.height && bimg.height < 800)) {
                                item.delete()
                                page.isDataProcessed = false
                                logger.severe("Page ${page.pid} deleted!")
                            }
                        } else
                            page.isDataProcessed = true

                        if (page.isDataProcessed)
                            if (item.validate()) {
                                if (page.order != order && !page.isGapPage) {
                                    val oldFile = item.asFile()
                                    val newFile = File(filePath.toString().replace(order.toString() + "_", page.order.toString() + "_"))
                                    if (!newFile.exists()) {
                                        oldFile.renameTo(newFile)
                                        logger.severe("Page ${page.pid} renamed!")
                                    }
                                }

                                item.page.isScanned = true
                            } else {
                                page.isDataProcessed = false
                                item.delete()
                                logger.severe("Page ${page.pid} deleted!")
                            }
                    } catch (e: IOException) {
                        // Значит файл с ошибкой
                        try {
                            item.delete()
                        } catch (e1: IOException) {
                            logger.severe("Cannot delete page ${page.pid}!")
                        }

                        page.isDataProcessed = false
                        logger.severe("Page ${page.pid} deleted!")
                    }

                    page.isFileExists = item.asFile().exists()
                } catch (pnf: PageNotFoundException) {
                    logger.severe("Page $fileName not found!")
                    try {
                        item.delete()
                        logger.severe("Page $fileName deleted!")
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun isPdfExists(): Boolean {
        return Files.list(storageDir.toPath())
                .filter { filePath -> Images.isPdfFile(filePath) }.count() == 1L
    }

    fun getOrCreatePdf(title: String): File {
        val pdfFiles = Files.list(storageDir.toPath()).filter { filePath -> Images.isPdfFile(filePath) }.collect(Collectors.toList())
        if (1 == pdfFiles.size)
            return pdfFiles[0].toFile()
        else {
            val pdfFile = File(storageDir.path + File.separator + title.replace("[^А-Яа-яa-zA-Z0-9-]".toRegex(), " ") + ".pdf")
            pdfFile.createNewFile()

            return pdfFile
        }
    }

    fun imgCount(): Long {
        return Files.list(storageDir.toPath()).filter { filePath -> Images.isImageFile(filePath) }.count()
    }

    companion object {
        val storages: MutableMap<String, LocalFSStorage> = emptyMap<String, LocalFSStorage>().toMutableMap()

        fun getStorage(storageDirName: String): LocalFSStorage {
            if (!storages.containsKey(storageDirName))
                storages[storageDirName] = LocalFSStorage(storageDirName)

            return storages[storageDirName]!!
        }
    }

    init {
        storageDir = File(storageDirName)
        logger = Logger.getLogger(LocalFSStorage::class.java, storageDirName)
    }
}
