package ru.kmorozov.db.core.logic.model.book.shpl

import com.google.gson.annotations.SerializedName
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.constants.ShplConstants
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage
import ru.kmorozov.gbd.core.logic.model.book.base.IPage

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
class ShplPage : AbstractPage {

    override fun compareTo(other: IPage): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val pid: String
        get() = id

    override val imgUrl: String
        get() = String.format("http://elib.shpl.ru/pages/%s/zooms/%s", id, GBDOptions.getImageWidth(ShplConstants.DEFAULT_PAGE_WIDTH))

    @SerializedName("id")
    private val id: String = ""
    @SerializedName("w")
    private val width: Int? = null
    @SerializedName("h")
    private val height: Int? = null
    @SerializedName("downloadUrl")
    private val downloadUrl: String? = null

    public override var order: Int = 0

    constructor() {}

    constructor(order: Int) {
        this.order = order
    }

    companion object {
        public val EMPTY_SHPL_PAGE : ShplPage = ShplPage()
    }
}
