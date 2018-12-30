package ru.kmorozov.gbd.core.logic.context

import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.gbd.core.loader.DirContextLoader
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo

/**
 * Created by sbt-morozov-kv on 02.12.2016.
 */
class ContextProvider(protected var loader: IContextLoader) : IContextLoader {
    override val empty: Boolean
        get() = false

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

    override fun getBookInfo(bookId: String): IBookInfo {
        return loader.getBookInfo(bookId)
    }

    override fun refreshContext() {
        loader.refreshContext()
    }

    companion object {

        private const val DB_CTX_PROVIDER_CLASS_NAME = "ru.kmorozov.library.data.loader.processors.gbd.DbContextLoader"

        private val LOCK_OBJ = Any()
        public var contextProvider: IContextLoader = getContextProviderInternal()

        private fun getContextProviderInternal(): IContextLoader {
            var _contextProvider: IContextLoader? = null
                synchronized(LOCK_OBJ) {
                    if (null == _contextProvider)
                        if (classExists(DB_CTX_PROVIDER_CLASS_NAME)) {
                            try {
                                _contextProvider = Class.forName(DB_CTX_PROVIDER_CLASS_NAME).getDeclaredConstructor().newInstance() as IContextLoader
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }

                    if (null == _contextProvider || !_contextProvider!!.isValid)
                        _contextProvider = ContextProvider(DirContextLoader.BOOK_CTX_LOADER)
                }

            _contextProvider!!.updateContext()

            return _contextProvider!!
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
