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

    protected override val bookUrl: String
        get() = bookId

    protected override fun extractBookInfo(doc: Document?): BookInfo {
        if (null == doc) return BookInfo.EMPTY_BOOK

        val title = doc.select("title")[0]
        val bookData = ShplBookData(title.textNodes()[0].text().split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
        lateinit var pagesInfo: ShplPagesInfo

        val scripts = doc.select("script")
        for (script in scripts) {
            val childs = script.childNodes()
            if (null != childs && !childs.isEmpty()) {
                val data = childs[0].toString()

                if (data.isEmpty()) continue

                if (data.contains(JSON_TAG_PAGES)) {
                    val pagesData = '['.toString() + data.split("[|]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[2].split("[\\[\\]]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[3] + ']'.toString()

                    val pages = Mapper.gson.fromJson(pagesData, Array<ShplPage>::class.java) as Array<IPage>
                    for (i in 1..pages.size)
                        (pages[i - 1] as ShplPage).order = i
                    pagesInfo = ShplPagesInfo(pages)
                    break
                }
            }
        }

        return BookInfo(bookData, pagesInfo, bookId)
    }

    companion object {

        private const val JSON_TAG_PAGES = "pages: "
    }
}
