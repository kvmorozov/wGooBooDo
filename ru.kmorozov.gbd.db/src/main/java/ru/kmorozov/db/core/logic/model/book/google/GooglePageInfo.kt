package ru.kmorozov.db.core.logic.model.book.google

import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.StringUtils
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.BOOK_ID_PLACEHOLDER
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.IMG_REQUEST_TEMPLATE
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.RQ_PG_PLACEHOLDER
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.RQ_SIG_PLACEHOLDER
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.RQ_WIDTH_PLACEHOLDER
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage
import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import java.io.Serializable

/**
 * Created by km on 21.11.2015.
 */

class GooglePageInfo : AbstractPage, Serializable, Comparable<IPage> {
    override val imgUrl: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    @SerializedName("pid")
    override var pid: String = ""
    @SerializedName("flags")
    private val flags: String? = null
    @SerializedName("title")
    private val title: String? = null
    var src: String? = null
    @SerializedName("uf")
    private val uf: String? = null
    @Transient
    public var sig: String? = null
        get() = if (null == field && null == src) null else src!!.substring(src!!.indexOf("sig=") + 4)
        private set

    @SerializedName("order")
    override var order: Int = 0
    @SerializedName("h")
    private val h: Int = 0
    private val width: Int = 0
    @SerializedName("links")
    private val links: Any? = null

    var prefix: String = ""
        get() = if (StringUtils.isEmpty(prefix)) parsePageNum() else field

    var pageNum = -1
        private set

    @Transient
    private var gapPage: Boolean = false

    constructor() {}

    // Создание страниц для заполнения разрыва
    constructor(pid: String, order: Int) {
        this.pid = pid
        this.order = order

        gapPage = true
    }

    fun getImqRqUrl(bookId: String, urlTemplate: String, width: Int): String {
        return urlTemplate.replace(BOOK_ID_PLACEHOLDER, bookId) + IMG_REQUEST_TEMPLATE.replace(RQ_PG_PLACEHOLDER, pid)
                .replace(RQ_SIG_PLACEHOLDER, sig!!)
                .replace(RQ_WIDTH_PLACEHOLDER, width.toString())
    }

    private fun parsePageNum(): String {
        var numFound = false
        prefix = ""
        val strNum = StringBuilder()
        var i = pid.length - 1
        while (0 <= i) {
            val ch = pid[i]
            if (!numFound && Character.isDigit(ch))
                strNum.insert(0, ch)
            else if (!numFound && !Character.isDigit(ch)) {
                numFound = true
                prefix = ch + prefix
            } else
                prefix = ch + prefix
            i--
        }

        pageNum = Integer.parseInt(strNum.toString())

        return prefix
    }

    override fun compareTo(other: IPage): Int {
        return this.order.compareTo(other.order)
    }
}