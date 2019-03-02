package ru.kmorozov.db.core.logic.model.book.google

import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair
import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo
import ru.kmorozov.gbd.core.logic.model.book.base.PageNotFoundException
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.gbd.logger.output.ReceiverProvider
import java.io.Serializable
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate
import java.util.function.Predicate.not

/**
 * Created by km on 21.11.2015.
 */
class GooglePagesInfo : IPagesInfo, Serializable {

    override val missingPagesList: String
        get() = getListByCondition(not(IPage::isFileExists))

    @SerializedName("page")
    override var pages: Array<IPage> = arrayOf<IPage>()
        set(pages: Array<IPage>) {
            field = pages
            this.pagesMap = HashMap()

            for (page in pages)
                pagesMap[page.pid] = page
        }


    @SerializedName("prefix")
    private val prefix: String? = null

    @Transient
    private var pagesMap: MutableMap<String, IPage> = HashMap()
    @Transient
    private var pagesList: LinkedList<IPage> = LinkedList()

    private fun addPage(page: IPage) {
        pagesMap[page.pid] = page
        pagesList.add(page)
    }

    override fun build() {
        val _pages = Arrays.asList(*pages)
        pagesMap = ConcurrentHashMap(_pages.size)
        pagesList = LinkedList()

        _pages.sortWith(Comparator { obj, anotherPage -> obj.compareTo(anotherPage) })

        var prevPage: GooglePageInfo? = null
        for (page in _pages) {
            addPage(page)

            if (null != prevPage && 1 < page.order - prevPage.order) fillGap(prevPage, page as GooglePageInfo)

            prevPage = page as GooglePageInfo
        }

        pages = pagesMap.values.toTypedArray()
    }

    private fun fillGap(beginGap: GooglePageInfo, endGap: GooglePageInfo) {
        val logger = Logger(ReceiverProvider.getReceiver(), "gapFinder", ": ")

        if (beginGap.isGapPage || endGap.isGapPage) return

        val beginPagePrefix = beginGap.prefix
        val endPagePrefix = endGap.prefix

        val beginPageNum = beginGap.pageNum
        val endPageNum = endGap.pageNum

        if (beginPageNum >= endPageNum && 1 < endPageNum && beginPagePrefix == endPagePrefix)
            logger.severe("Cannot fill gap between pages ${beginGap.pid}(order=${beginGap.order}) and ${endGap.pid}(order=${endGap.order})")

        if (beginPagePrefix == endPagePrefix) {
            for (index in beginGap.order + 1 until endGap.order) {
                val pid = if (0 < beginPageNum) beginPagePrefix + (beginPageNum + index - beginGap.order) else beginGap.pid + '_'.toString() + index
                val gapPage = GooglePageInfo(pid, index)
                addPage(gapPage)
            }

            if (0 < beginPageNum && 0 < endPageNum)
                for (index in beginGap.order + 1 until endGap.order - endPageNum) {
                    val gapPage = GooglePageInfo(beginPagePrefix + (index + 1), index)
                    addPage(gapPage)
                }
        } else {
            if (1 <= endPageNum) {
                var pagesToCreate = endGap.order - beginGap.order - 1
                var pagesCreated = 0
                for (index in 1..pagesToCreate) {
                    if (1 > endPageNum - index) break
                    val newPagePidFromEnd = endPagePrefix + (endPageNum - index)
                    if (!pagesMap.containsKey(newPagePidFromEnd) && !beginPagePrefix.contains(newPagePidFromEnd)) {
                        val gapPage = GooglePageInfo(newPagePidFromEnd, endGap.order - index)
                        addPage(gapPage)
                        pagesCreated++
                    } else
                        break
                }
                if (pagesCreated < pagesToCreate) {
                    pagesToCreate -= pagesCreated
                    for (index in 1..pagesToCreate) {
                        val newPagePidFromBegin = beginPagePrefix + (beginPageNum + index)
                        val gapPage = GooglePageInfo(newPagePidFromBegin, beginGap.order + index)
                        addPage(gapPage)
                    }
                }
            } else if (1 < beginPageNum && 0 > endPageNum) {
                logger.severe("Cannot fill gap between pages ${beginGap.pid}(order=${beginGap.order}) and ${endGap.pid}(order=${endGap.order})")
            }
        }

        pagesList.sortWith(Comparator { obj, anotherPage -> obj.compareTo(anotherPage) })
    }

    override fun getPageByPid(pid: String): GooglePageInfo {
        if (!pagesMap.containsKey(pid))
            throw PageNotFoundException()

        return pagesMap[pid] as GooglePageInfo
    }

    private fun createPair(p1: IPage, p2: IPage): Pair<IPage, IPage> {
        return if (p1.order < p2.order) ImmutablePair(p1, p2) else ImmutablePair(p2, p1)
    }

    private fun getListByCondition(condition: Predicate<IPage>): String {
        val bList = StringBuilder()
        val pairs = ArrayList<Pair<IPage, IPage>>()

        var blockStart: IPage? = null
        var prevPage: IPage? = null

        val filteredCount = pagesList.stream().filter(condition).count()
        val lastPage = pagesList.last

        for (currentPage in pagesList) {
            if (condition.test(currentPage))
                if (null == blockStart) {
                    blockStart = currentPage
                } else {
                }
            else {
                if (null == blockStart) {
                } else {
                    pairs.add(createPair(blockStart, prevPage!!))
                    blockStart = null
                }
            }

            if (currentPage == lastPage && null != blockStart) pairs.add(createPair(blockStart, currentPage))

            prevPage = currentPage
        }

        for (pair in pairs)
            if (pair.left === pair.right)
                bList.append("${pair.left.pid}, ")
            else
                bList.append("${pair.left.pid}-${pair.right.pid}, ")

        if (0 < bList.length) {
            bList.deleteCharAt(bList.length - 1).deleteCharAt(bList.length - 1)
            bList.append(String.format(". Total = %d/%d", filteredCount, pagesList.size))
        }

        return bList.toString()
    }
}
