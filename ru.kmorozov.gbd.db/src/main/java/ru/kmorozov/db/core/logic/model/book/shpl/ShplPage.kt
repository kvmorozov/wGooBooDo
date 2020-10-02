package ru.kmorozov.db.core.logic.model.book.shpl

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.constants.ShplConstants
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
class ShplPage : AbstractPage {

    override val pid: String
        get() = id

    override val imgUrl: String
        get() = "http://elib.shpl.ru/pages/$id/zooms/${GBDOptions.getImageWidth(ShplConstants.DEFAULT_PAGE_WIDTH)}"

    @Expose
    @SerializedName("id")
    private val id: String = ""

    @Expose
    @SerializedName("w")
    private val width: Int? = null

    @Expose
    @SerializedName("h")
    private val height: Int? = null

    @Expose
    @SerializedName("downloadUrl")
    private val downloadUrl: String? = null

    public override var order: Int = 0

    constructor() {}

    constructor(order: Int) {
        this.order = order
    }

    companion object {
        public val EMPTY_SHPL_PAGE: ShplPage = ShplPage()
    }
}
