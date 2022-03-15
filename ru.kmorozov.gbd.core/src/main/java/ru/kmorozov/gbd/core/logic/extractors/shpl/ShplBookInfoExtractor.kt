package ru.kmorozov.gbd.core.logic.extractors.shpl

import org.jsoup.nodes.Document
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.db.core.logic.model.book.shpl.ShplBookData
import ru.kmorozov.db.core.logic.model.book.shpl.ShplPage
import ru.kmorozov.db.core.logic.model.book.shpl.ShplPagesInfo
import ru.kmorozov.db.utils.Mapper
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookInfoExtractor
import ru.kmorozov.gbd.core.logic.model.book.base.IPage

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
class ShplBookInfoExtractor(bookId: String) : AbstractBookInfoExtractor(bookId) {

    override val bookUrl: String
        get() = bookId

    override fun extractBookInfo(doc: Document?): BookInfo {
        if (null == doc) return BookInfo.EMPTY_BOOK

        val bookData = ShplBookData(doc.select("title")[0].text().replace("|", ""))
        lateinit var pagesInfo: ShplPagesInfo

        val scripts = doc.select("script")
        for (script in scripts) {
            val childs = script.childNodes()
            if (childs.isNotEmpty()) {
                val data = childs[0].toString()

                if (data.isEmpty()) continue

                if (data.contains(JSON_TAG_PAGES)) {
                    val pagesData = '['.toString() + data.split("[")[2].split("]")[0] + ']'.toString()

                    val pages = Mapper.gson.fromJson(pagesData, Array<IPage>::class.java)
                    for (i in 1..pages.size)
                        pages[i - 1].order = i
                    pagesInfo = ShplPagesInfo(pages)
                    break
                }
            }
        }

        return BookInfo(bookData, pagesInfo, bookId)
    }

    companion object {

        private const val JSON_TAG_PAGES = "\"pages\":"
    }
}
