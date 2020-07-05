package ru.kmorozov.library.data.loader.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.library.data.loader.impl.LoaderExecutor.State
import ru.kmorozov.library.data.loader.utils.ConsistencyUtils
import ru.kmorozov.library.data.loader.utils.WindowsShortcut
import ru.kmorozov.library.data.model.book.Book
import ru.kmorozov.library.data.model.book.BookInfo
import ru.kmorozov.library.data.model.book.LinkInfo
import ru.kmorozov.library.data.model.book.Storage
import ru.kmorozov.library.data.model.dto.ItemDTO
import ru.kmorozov.library.data.server.condition.StorageEnabledCondition
import ru.kmorozov.onedrive.TaskQueue
import ru.kmorozov.onedrive.client.OneDriveItem
import ru.kmorozov.onedrive.client.OneDriveProvider
import ru.kmorozov.onedrive.client.downloader.ResumableDownloader
import ru.kmorozov.onedrive.client.downloader.ResumableDownloaderProgressListener
import ru.kmorozov.onedrive.client.walker.OneDriveWalkers
import ru.kmorozov.onedrive.filesystem.FileSystemProvider.FACTORY
import ru.kmorozov.onedrive.tasks.DownloadTask
import ru.kmorozov.onedrive.tasks.Task.TaskOptions
import ru.kmorozov.onedrive.tasks.TaskReporter
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.text.ParseException
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Stream

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */

@Component
@Conditional(StorageEnabledCondition::class)
open class OneDriveLoader : StoredLoader() {

    @Autowired
    @Lazy
    private lateinit var api: OneDriveProvider

    @Throws(IOException::class)
    override fun load() {
        load { false }
    }

    @Throws(IOException::class)
    fun load(skipCondition: (OneDriveItem) -> Boolean) {
        state = State.STARTED

        logger.info("Sync db started.")

        OneDriveWalkers.walk(api, skipCondition).forEach { oneDriveItem ->
            if (isStopped)
                OneDriveWalkers.stopAll()

            if (oneDriveItem.isDirectory && !isStopped) {
                val category = getCategoryByServerItem(ServerItem(oneDriveItem))
                for (storage in category!!.getStorages()!!)
                    try {
                        updateStorage(storage)
                    } catch (e: IOException) {
                        logger.error("Error when updating storage: " + e.message)
                    }

            }
        }

        logger.info("Sync db completed.")

        state = State.STOPPED
    }

    @Throws(IOException::class)
    override fun getItemsStreamByStorage(storage: Storage): Stream<ServerItem> {
        val children = api.getChildren(storage.url)
        return Arrays.stream(children).map { ServerItem(it) }
    }

    override fun processLinks() {
        val lnkBooks = booksRepository.streamByBookInfoFormat(BookInfo.BookFormat.LNK)

        lnkBooks.forEach { this.resolveLink(it) }
    }

    override fun resolveLink(lnkBook: Book) {
        if (null != lnkBook.linkInfo || !lnkBook.isLink)
            return

        logger.info("Resolving link: " + lnkBook.bookInfo.fileName!!)

        try {
            val linkItem = api.getItem(lnkBook.bookInfo.path!!)
            val linkInfo = LinkInfo()

            val tmpFile = File.createTempFile("one", ".lnk")
            api.download(linkItem, tmpFile, object : ResumableDownloaderProgressListener {
                override fun progressChanged(downloader: ResumableDownloader) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            })
            if (WindowsShortcut.isPotentialValidLink(tmpFile))
                try {
                    val lnkFile = WindowsShortcut(tmpFile, Charset.forName("Windows-1251"))
                    if (lnkFile.isDirectory) {
                        val linkedStorage = getStorageByLink(lnkFile.realFilename!!)
                        if (null == linkedStorage) {
                            linkInfo.isBroken = true
                            logger.warn("Storage lnk not found for " + lnkFile.realFilename!!)
                        } else {
                            val thisStorage = lnkBook.storage
                            linkInfo.linkedStorage = linkedStorage

                            if (null != thisStorage) {
                                val linkCategory = linkedStorage.mainCategory
                                linkCategory!!.addParent(thisStorage.mainCategory!!)
                                categoryRepository.save(linkCategory)
                            }
                        }
                    } else {
                        val realPath = lnkFile.realFilename
                        val linkedBook = getBookByLink(realPath!!)
                        if (null == linkedBook) {
                            linkInfo.isBroken = true
                            logger.warn("File lnk not found for $realPath")
                        } else
                            linkInfo.linkedBook = linkedBook
                    }
                } catch (e: ParseException) {
                    logger.error(e)
                }

            tmpFile.delete()

            if (null != linkInfo.linkedBook || null != linkInfo.linkedStorage || linkInfo.isBroken) {
                lnkBook.linkInfo = linkInfo
                booksRepository.save(lnkBook)
            }
        } catch (ioe: IOException) {
            logger.error(ioe)
        }

    }

    private fun getBookByLink(lnkFileName: String): Book? {
        val names = lnkFileName.split(delimiter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val books = booksRepository.findAllByBookInfoFileName(names[names.size - 1])

        return if (1 == books.size) books[0] else null
    }

    override fun postponedLinksLoad(): Boolean {
        return true
    }

    private fun getStorageByLink(lnkFileName: String): Storage? {
        val names = lnkFileName.split(delimiter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var storages = storageRepository.findAllByName(names[names.size - 1])
        var parentName: String?

        var index = names.size - 1
        while (0 < index) {
            if (1 == storages.size)
                break
            else {
                parentName = names[index - 1]
                val filteredStorages = ArrayList<Storage>()
                for (storage in storages)
                    if (storage.parent!!.name == parentName)
                        filteredStorages.add(storage)

                storages = filteredStorages
            }
            index--
        }

        return if (1 == storages.size) storages[0] else null
    }

    override fun refresh(storage: Storage): Storage {
        if (ItemDTO.REFRESH_INTERVAL.toLong() > System.currentTimeMillis() - storage.storageInfo!!.lastChecked)
            return storage

        try {
            val item = api.getItem(storage.url)
            val serverItem = ServerItem(item)

            fillStorage(storage, serverItem)

            val books = ConsistencyUtils.deduplicate(booksRepository.findAllByStorage(storage), booksRepository)
                    .associateBy({ it.bookInfo.path }, { it })
            val children = Arrays.asList(*api.getChildren(item)).associateBy({ it.id }, { it })

            storageRepository.save(storage)
        } catch (e: IOException) {
            logger.error(e)
        }

        return storage
    }

    override fun downloadBook(book: Book) {
        try {
            val bookItem = api.getItem(book.bookInfo.path!!)
            val parent = File(DEFAULT_PARENT)

            book.storage!!.localPath = DEFAULT_PARENT
            storageRepository.save(book.storage!!)

            val itemPartSize = if (0L < bookItem.size) bookItem.size.toInt() / 5 else Integer.MAX_VALUE

            val task = DownloadTask(
                    TaskOptions(TaskQueue(), api, FACTORY.readWriteProvider(), TaskReporter()),
                    parent, bookItem, true, itemPartSize)

            task.run()
        } catch (e: IOException) {
            logger.error(e)
        }

    }

    companion object {

        private val logger = Logger.getLogger(OneDriveLoader::class.java)
        private val delimiter = Pattern.quote(File.separator)
        private const val DEFAULT_PARENT = "E:\\tmp"
    }
}
