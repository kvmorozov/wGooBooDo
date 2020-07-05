package ru.kmorozov.library.data.loader.impl

import org.springframework.beans.factory.annotation.Autowired
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.library.data.model.book.*
import ru.kmorozov.library.data.repository.BooksRepository
import ru.kmorozov.library.data.repository.CategoryRepository
import ru.kmorozov.library.data.repository.StorageRepository
import ru.kmorozov.library.utils.BookUtils
import java.io.IOException
import java.util.function.Predicate
import java.util.stream.Stream

abstract class StoredLoader : BaseLoader() {

    @Autowired
    protected lateinit var categoryRepository: CategoryRepository

    @Autowired
    protected lateinit var storageRepository: StorageRepository

    @Autowired
    protected lateinit var booksRepository: BooksRepository

    fun clear() {
        val categoryCount = categoryRepository.count()
        val storageCount = storageRepository.count()
        val booksCount = booksRepository.count()

        if (0L < categoryCount) {
            logger.info("Categories loaded: $categoryCount")
            categoryRepository.deleteAll()
        }

        if (0L < storageCount) {
            logger.info("Storages loaded: $storageCount")
            storageRepository.deleteAll()
        }

        if (0L < booksCount) {
            logger.info("Books loaded: $storageCount")
            booksRepository.deleteAll()
        }
    }

    private fun getOrCreateCategory(name: String): Category {
        var category: Category? = categoryRepository.findOneByName(name)
        if (null == category) {
            category = Category()
            category.name = name

            categoryRepository.save(category)
        }

        return category
    }

    private fun getOrCreateStorage(serverItem: ServerItem): Storage {
        val storage = storageRepository.findByUrl(serverItem.url)
        return fillStorage(storage ?: Storage(), serverItem)
    }

    protected fun getCategoryByServerItem(serverItem: ServerItem): Category? {
        val category = getOrCreateCategory(serverItem.name)
        val storage = getOrCreateStorage(serverItem)

        storage.addCategory(category)

        storageRepository.save(storage)

        val parentStorage = if (null == serverItem.parent) null else storageRepository.findByUrl(serverItem.parent!!.url)

        if (null != parentStorage) {
            storage.parent = parentStorage
            storageRepository.save(storage)

            category.addParents(parentStorage.getCategories()!!)
            categoryRepository.save(category)
        }

        category.addStorage(storage)
        categoryRepository.save(category)

        return storage.mainCategory
    }

    @Throws(IOException::class)
    protected abstract fun getItemsStreamByStorage(storage: Storage): Stream<ServerItem>

    abstract fun processLinks()

    abstract fun postponedLinksLoad(): Boolean

    @Throws(IOException::class)
    protected fun updateStorage(storage: Storage) {
        val storageInfo = if (null == storage.storageInfo) StorageInfo() else storage.storageInfo

        getItemsStreamByStorage(storage)
                .filter(Predicate<ServerItem> { it.isLoadableOrLink })
                .forEach { serverItem ->
                    if (!serverItem.isDirectory) {
                        val bookFormat = BookUtils.getFormat(serverItem.name)
                        if (BookInfo.BookFormat.UNKNOWN !== bookFormat) {
                            val existBook = booksRepository.findOneByBookInfoPath(serverItem.url)
                            if (null == existBook) {
                                val book = Book()

                                val bookInfo = BookInfo()
                                bookInfo.fileName = serverItem.name
                                bookInfo.path = serverItem.url
                                bookInfo.format = bookFormat
                                bookInfo.lastModifiedDateTime = serverItem.lastModifiedDateTime
                                bookInfo.size = serverItem.size

                                book.bookInfo = bookInfo
                                book.storage = storage

                                if (book.isLink && !postponedLinksLoad()) {
                                    links.add(serverItem.originalItem)
                                } else {
                                    booksRepository.save(book)
                                    storage.storageInfo!!.incFilesCount()
                                }
                            } else {
                                val oldDate = existBook.bookInfo.lastModifiedDateTime
                                val newDate = serverItem.lastModifiedDateTime
                                val dateCondition = null == oldDate || oldDate.before(newDate)

                                val oldSize = existBook.bookInfo.size
                                val newSize = serverItem.size
                                val sizeCondition = 0L == oldSize || oldSize != newSize

                                val storageCondition = existBook.storage != storage
                                val nameCondition = serverItem.name == existBook.bookInfo.fileName

                                if (dateCondition || sizeCondition || storageCondition || nameCondition) {
                                    existBook.bookInfo.fileName = serverItem.name
                                    existBook.bookInfo.lastModifiedDateTime = newDate
                                    existBook.bookInfo.size = newSize
                                    existBook.storage = storage
                                    booksRepository.save(existBook)
                                }
                            }
                        }
                    }
                }

        storageInfo!!.lastChecked = System.currentTimeMillis()

        storage.storageInfo = storageInfo
        storageRepository.save(storage)
    }

    companion object {

        private val logger = Logger.getLogger(StoredLoader::class.java)

    }

    protected fun fillStorage(storage: Storage, serverItem: ServerItem): Storage {
        storage.storageType = serverItem.storageType
        storage.url = serverItem.url
        storage.name = serverItem.name
        storage.lastModifiedDateTime = serverItem.lastModifiedDateTime
        storage.storageInfo = StorageInfo(serverItem.filesCount)
        storage.storageInfo!!.lastChecked = System.currentTimeMillis()

        return storage
    }
}
