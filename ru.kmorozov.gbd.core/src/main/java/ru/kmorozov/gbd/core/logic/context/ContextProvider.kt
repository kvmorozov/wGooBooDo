package ru.kmorozov.gbd.core.logic.context

import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.gbd.core.loader.DirContextLoader
import ru.kmorozov.db.core.logic.model.book.BookInfo

/**
 * Created by sbt-morozov-kv on 02.12.2016.
 */
class ContextProvider(protected var loader: IContextLoader) : IContextLoader {

    override val bookIdsList: Set<String>
        get() = loader.bookIdsList

    override val contextSize: Int
        get() = loader.contextSize

    override val isValid: Boolean
        get() = loader.isValid

    override fun updateIndex() {
        loader.updateIndex()
    }

    override fun updateContext() {
        loader.updateContext()
    }

    override fun updateBookInfo(bookInfo: BookInfo) {
        loader.updateBookInfo(bookInfo)
    }

    override fun getBookInfo(bookId: String): BookInfo {
        return loader.getBookInfo(bookId)
    }

    override fun refreshContext() {
        loader.refreshContext()
    }

    companion object {

        private const val DB_CTX_PROVIDER_CLASS_NAME = "ru.kmorozov.library.data.loader.processors.gbd.DbContextLoader"

        private val LOCK_OBJ = Any()
        @Volatile
        private var contextProvider: IContextLoader? = null

        fun getContextProvider(): IContextLoader? {
            if (null == contextProvider) {
                synchronized(LOCK_OBJ) {
                    if (null == contextProvider)
                        if (classExists(DB_CTX_PROVIDER_CLASS_NAME)) {
                            try {
                                contextProvider = Class.forName(DB_CTX_PROVIDER_CLASS_NAME).getDeclaredConstructor().newInstance() as IContextLoader
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }

                    if (null == contextProvider || !contextProvider!!.isValid)
                        contextProvider = ContextProvider(DirContextLoader.BOOK_CTX_LOADER)
                }

                contextProvider!!.updateContext()
            }

            return contextProvider
        }

        fun setDefaultContextProvider(_contextProvider: IContextLoader) {
            contextProvider = _contextProvider
        }

        private fun classExists(className: String): Boolean {
            try {
                Class.forName(className)
                return true
            } catch (cnfe: ClassNotFoundException) {
                return false
            }

        }
    }
}
