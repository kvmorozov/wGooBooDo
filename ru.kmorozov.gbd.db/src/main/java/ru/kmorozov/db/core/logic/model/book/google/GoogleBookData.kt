package ru.kmorozov.db.core.logic.model.book.google

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

    @SerializedName("fullview")
    private val fullview: Boolean = false
    @SerializedName("page_width")
    private val pageWidth: Int = 0
    @SerializedName("page_height")
    private val pageHeight: Int = 0
    @SerializedName("font_height")
    private val fontHeight: Int = 0
    @SerializedName("first_content_page")
    private val firstContentPage: Int = 0
    @SerializedName("disable_twopage")
    private val disableTwopage: Boolean = false
    @SerializedName("initial_zoom_width_override")
    private val initialZoomWidthOverride: Int = 0
    @SerializedName("show_print_pages_button")
    private val showPrintPagesButton: Boolean = false
    @SerializedName("title")
    override val title: String? = null
    @SerializedName("subtitle")
    private val subtitle: String? = null
    @SerializedName("attribution")
    private val attribution: String? = null
    @SerializedName("additional_info")
    private val additionalInfo: AdditionalInfo? = null
    @SerializedName("table_of_contents_page_id")
    private val tableOfContentsPageId: String? = null
    @SerializedName("max_resolution_image_width")
    private val maxResolutionImageWidth: Int = 0
    @SerializedName("max_resolution_image_height")
    private val maxResolutionImageHeight: Int = 0
    @SerializedName("num_toc_pages")
    private val numTocPages: Int = 0
    @SerializedName("quality_info")
    private val qualityInfo: String? = null
    @SerializedName("volume_id")
    override val volumeId: String? = null
    @SerializedName("permission_info")
    private val permissionInfo: String? = null
    @SerializedName("is_ebook")
    private val ebook: Boolean = false
    @SerializedName("volumeresult")
    val flags: VolumeResultFlags? = null
    @SerializedName("rating")
    private val rating: Int = 0
    @SerializedName("num_reviews")
    private val numReviews: Int = 0
    @SerializedName("publisher")
    private val publisher: String? = null
    @SerializedName("publication_date")
    private val publicationDate: String? = null
    @SerializedName("num_pages")
    private val numPages: Int = 0
    @SerializedName("sample_url")
    private val sampleUrl: String? = null
    @SerializedName("synposis")
    private val synposis: String? = null
    @SerializedName("my_library_url")
    private val myLibraryUrl: String? = null
    @SerializedName("is_magazine")
    private val magazine: Boolean = false
    @SerializedName("is_public_domain")
    private val publicDomain: Boolean = false
    @SerializedName("last_page")
    private val lastPage: GooglePageInfo? = null

    val baseUrl: URI
        get() = URI.create(HTTPS_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, volumeId!!))

}
