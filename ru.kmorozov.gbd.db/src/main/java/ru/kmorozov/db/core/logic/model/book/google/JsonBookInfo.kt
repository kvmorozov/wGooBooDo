package ru.kmorozov.db.core.logic.model.book.google

import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * Created by km on 28.11.2015.
 */
internal class JsonBookInfo : Serializable {

    @SerializedName("BuyLinks")
    private val buyLinks: Array<BuyLink>? = null
    @SerializedName("AboutUrl")
    private val aboutUrl: String? = null
    @SerializedName("PreviewUrl")
    private val previewUrl: String? = null
    @SerializedName("allowed_syndication_flags")
    private val flags: SyndicationFlags? = null
    @SerializedName("online_access")
    private val onlineAccess: OnlineAccess? = null
    @SerializedName("TocLine")
    private val toc: Array<TocItem>? = null
}
