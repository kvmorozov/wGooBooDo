package ru.kmorozov.db.core.logic.model.book.google

import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * Created by km on 28.11.2015.
 */
class VolumeResultFlags : Serializable {

    @SerializedName("has_flowing_text")
    private val hasFlowingText: Boolean = false
    @SerializedName("has_scanned_text")
    private val hasScannedText: Boolean = false
    @SerializedName("can_download_pdf")
    private val canDownloadPdf: Boolean = false
    @SerializedName("can_download_epub")
    private val canDownloadEpub: Boolean = false
    @SerializedName("is_pdf_drm_enabled")
    private val pdfDrmEnabled: Boolean = false
    @SerializedName("is_epub_drm_enabled")
    private val epubDrmEnabled: Boolean = false
    @SerializedName("download_pdf_url")
    val downloadPdfUrl: String? = null
}
