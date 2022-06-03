package ru.kmorozov.db.core.logic.model.book.google

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData

import java.io.Serializable
import java.net.URI

import ru.kmorozov.gbd.core.config.constants.GoogleConstants.BOOK_ID_PLACEHOLDER
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.HTTPS_TEMPLATE

/**
 * Created by km on 28.11.2015.
 */
class GoogleBookData : IBookData, Serializable {

    @Expose @SerializedName("fullview")
    private val fullview: Boolean = false
    @Expose @SerializedName("page_width")
    private val pageWidth: Int = 0
    @Expose @SerializedName("page_height")
    private val pageHeight: Int = 0
    @Expose @SerializedName("font_height")
    private val fontHeight: Int = 0
    @Expose @SerializedName("first_content_page")
    private val firstContentPage: Int = 0
    @Expose @SerializedName("disable_twopage")
    private val disableTwopage: Boolean = false
    @Expose @SerializedName("initial_zoom_width_override")
    private val initialZoomWidthOverride: Int = 0
    @Expose @SerializedName("show_print_pages_button")
    private val showPrintPagesButton: Boolean = false
    @Expose @SerializedName("title")
    override lateinit var title: String
    @Expose @SerializedName("subtitle")
    private val subtitle: String? = null
    @Expose @SerializedName("attribution")
    private val attribution: String? = null
    @Expose @SerializedName("additional_info")
    val additionalInfo: AdditionalInfo? = null
    @Expose @SerializedName("table_of_contents_page_id")
    val tableOfContentsPageId: String? = null
    @Expose @SerializedName("max_resolution_image_width")
    private val maxResolutionImageWidth: Int = 0
    @Expose @SerializedName("max_resolution_image_height")
    private val maxResolutionImageHeight: Int = 0
    @Expose @SerializedName("num_toc_pages")
    private val numTocPages: Int = 0
    @Expose @SerializedName("quality_info")
    private val qualityInfo: String? = null
    @Expose @SerializedName("volume_id")
    override lateinit var volumeId: String
    @Expose @SerializedName("permission_info")
    private val permissionInfo: String? = null
    @Expose @SerializedName("is_ebook")
    private val ebook: Boolean = false
    @Expose @SerializedName("volumeresult")
    val flags: VolumeResultFlags? = null
    @Expose @SerializedName("rating")
    private val rating: Int = 0
    @Expose @SerializedName("num_reviews")
    private val numReviews: Int = 0
    @Expose @SerializedName("publisher")
    val publisher: String? = null
    @Expose @SerializedName("publication_date")
    val publicationDate: String? = null
    @Expose @SerializedName("num_pages")
    val numPages: Int = 0
    @Expose @SerializedName("sample_url")
    private val sampleUrl: String? = null
    @Expose @SerializedName("synposis")
    private val synposis: String? = null
    @Expose @SerializedName("my_library_url")
    private val myLibraryUrl: String? = null
    @Expose @SerializedName("is_magazine")
    private val magazine: Boolean = false
    @Expose @SerializedName("is_public_domain")
    private val publicDomain: Boolean = false
    @Expose @SerializedName("last_page")
    private val lastPage: GooglePageInfo? = null

    val baseUrl: URI
        get() = URI.create(HTTPS_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, volumeId))

}
