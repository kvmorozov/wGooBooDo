package ru.kmorozov.library.data.server.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Lazy
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.library.data.loader.LoaderConfiguration
import ru.kmorozov.library.data.loader.impl.LoaderExecutor
import ru.kmorozov.library.data.loader.processors.DuplicatesProcessor
import ru.kmorozov.library.data.loader.processors.JstorProcessor
import ru.kmorozov.library.data.loader.processors.gbd.GbdRemoteProcessor
import ru.kmorozov.library.data.loader.utils.BookUtils
import ru.kmorozov.library.data.model.IDataRestServer
import ru.kmorozov.library.data.model.book.BookInfo
import ru.kmorozov.library.data.model.dto.*
import ru.kmorozov.library.data.repository.BooksRepository
import ru.kmorozov.library.data.repository.StorageRepository
import ru.kmorozov.library.data.server.condition.StorageEnabledCondition
import java.util.*
import java.util.stream.Collectors

@RestController
@EnableMongoRepositories(basePackages = arrayOf("ru.kmorozov.library.data.repository"))
@ComponentScan(basePackageClasses = arrayOf(LoaderConfiguration::class, LoaderExecutor::class, DuplicatesProcessor::class, GbdRemoteProcessor::class, LoaderExecutor::class, JstorProcessor::class))
@Conditional(StorageEnabledCondition::class)
class StorageController : IDataRestServer {

    @Autowired
    @Lazy
    private lateinit var storageRepository: StorageRepository

    @Autowired
    @Lazy
    private lateinit var booksRepository: BooksRepository

    @Autowired
    @Lazy
    private lateinit var loader: LoaderExecutor

    @Autowired
    @Lazy
    private lateinit var duplicatesProcessor: DuplicatesProcessor

    @Autowired
    @Lazy
    private lateinit var jstorProcessor: JstorProcessor

    @Autowired
    @Lazy
    private lateinit var gbdProcessor: GbdRemoteProcessor

    @RequestMapping("/login")
    override fun login(@RequestParam(name = "login") login: String): UserDTO {
        return UserDTO(login)
    }

    @RequestMapping("/jstorUpdate")
    fun jstorUpdate() {
        jstorProcessor.process()
    }

    @RequestMapping("/storagesByParentId")
    override fun getStoragesByParentId(@RequestParam(name = "storageId") storageId: String): List<StorageDTO> {
        val parentStorage = if (StringUtils.isEmpty(storageId)) null else storageRepository.findById(storageId).get()

        val realStorages = storageRepository.findAllByParent(parentStorage!!)
        val linksInStorages = booksRepository.findAllByStorageAndBookInfoFormat(parentStorage, BookInfo.BookFormat.LNK)
        linksInStorages.forEach { book -> loader.resolveLink(book) }
        val linkedStorages = linksInStorages.stream()
                .filter { lnk -> null != lnk.linkInfo && null != lnk.linkInfo!!.linkedStorage }
                .map { lnk -> lnk.linkInfo!!.linkedStorage }
                .collect(Collectors.toList()).filterNotNull()

        realStorages.addAll(linkedStorages)

        return realStorages.stream().map<StorageDTO> { StorageDTO(it) }.collect(Collectors.toList())
    }

    @RequestMapping("/booksByStorageId")
    override fun getBooksByStorageId(@RequestParam(name = "storageId") storageId: String): List<BookDTO> {
        val storage = (if (StringUtils.isEmpty(storageId)) null else storageRepository.findById(storageId).get())
                ?: return emptyList()

        return booksRepository.findAllByStorage(storage).stream()
                .filter { book -> !book.isBrokenLink }
                .map { book -> if (book.isLink) book.linkInfo!!.linkedBook else book }
                .filter { Objects.nonNull(it) }
                .map<BookDTO> { BookUtils.createBookDIO(it!!) }
                .collect(Collectors.toList())
    }

    @RequestMapping("/itemsByStorageId")
    override fun getItemsByStorageId(storageId: String): List<ItemDTO> {
        val result = getBooksByStorageId(storageId).stream().map<ItemDTO> { ItemDTO(it) }.collect(Collectors.toList())
        result.addAll(getStoragesByParentId(storageId).stream().map<ItemDTO> { ItemDTO(it) }.collect(Collectors.toList()))

        return result
    }

    @RequestMapping("/item")
    override fun getItem(@RequestParam(name = "itemId") itemId: String, @RequestParam(name = "itemType") itemType: ItemDTO.ItemType, @RequestParam(name = "refresh") refresh: Boolean): ItemDTO? {
        when (itemType) {
            ItemDTO.ItemType.book -> return ItemDTO(BookDTO(booksRepository.findById(itemId).get(), true))
            ItemDTO.ItemType.storage -> {
                var storage = storageRepository.findById(itemId).get()
                if (refresh)
                    storage = loader.refresh(storage)
                val item = ItemDTO(StorageDTO(storage, true))
                if (refresh)
                    item.setUpdated()

                return item
            }
            else -> return null
        }
    }

    @RequestMapping(value = ["/updateLibrary"], method = arrayOf(RequestMethod.POST))
    override fun updateLibrary(@RequestParam(name = "state") stateString: String) {
        val state = LoaderExecutor.State.valueOf(stateString)

        when (state) {
            LoaderExecutor.State.STARTED -> loader.start()
            LoaderExecutor.State.PAUSED -> loader.pause()
            LoaderExecutor.State.STOPPED -> loader.stop()
        }
    }

    @RequestMapping("/downloadBook")
    override fun downloadBook(@RequestParam(name = "bookId") bookId: String): BookDTO {
        val book = booksRepository.findById(bookId).get()
        if (BookUtils.bookLoaded(book))
            return BookDTO(book, true)
        else {
            book.storage!!.localPath = ""
            booksRepository.save(book)
        }

        loader.downloadBook(book)
        return BookDTO(book, true)
    }

    @RequestMapping("/findDuplicates")
    override fun findDuplicates(): List<DuplicatedBookDTO> {
        return duplicatesProcessor.findDuplicates()
    }

    @RequestMapping("/synchronizeDb")
    override fun synchronizeDb() {
        loader.start()
    }

    @RequestMapping("/processDuplicates")
    fun processDuplicates() {
        duplicatesProcessor.process()
    }

    @RequestMapping("/gbdLoadRemote")
    fun gbdLoad(@RequestParam(name = "bookId", required = false) bookId: String) {
        gbdProcessor.load(bookId)
    }

    companion object {

        protected val logger = Logger.getLogger(StorageController::class.java)
    }

}
