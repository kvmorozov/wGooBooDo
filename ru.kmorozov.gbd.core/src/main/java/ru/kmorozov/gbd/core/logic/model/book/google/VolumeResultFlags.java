package ru.kmorozov.gbd.core.logic.model.book.google;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by km on 28.11.2015.
 */
public class VolumeResultFlags implements Serializable {

    @SerializedName("has_flowing_text") private boolean hasFlowingText;
    @SerializedName("has_scanned_text") private boolean hasScannedText;
    @SerializedName("can_download_pdf") private boolean canDownloadPdf;
    @SerializedName("can_download_epub") private boolean canDownloadEpub;
    @SerializedName("is_pdf_drm_enabled") private boolean pdfDrmEnabled;
    @SerializedName("is_epub_drm_enabled") private boolean epubDrmEnabled;
    @SerializedName("download_pdf_url") private String downloadPdfUrl;

    public String getDownloadPdfUrl() {
        return downloadPdfUrl;
    }
}
