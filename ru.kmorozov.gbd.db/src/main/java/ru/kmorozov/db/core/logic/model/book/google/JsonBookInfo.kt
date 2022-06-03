package ru.kmorozov.db.core.logic.model.book.google

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * Created by km on 28.11.2015.
 */
class JsonBookInfo : Serializable {

    @Expose @SerializedName("BuyLinks")
    private val buyLinks: Array<BuyLink>? = null
    @Expose @SerializedName("AboutUrl")
    private val aboutUrl: String? = null
    @Expose @SerializedName("PreviewUrl")
    private val previewUrl: String? = null
    @Expose @SerializedName("allowed_syndication_flags")
    private val flags: SyndicationFlags? = null
    @Expose @SerializedName("online_access")
    private val onlineAccess: OnlineAccess? = null
    @Expose @SerializedName("TocLine")
    val toc: Array<TocItem>? = null
}
