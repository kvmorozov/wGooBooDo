package ru.simpleGBD.App.Logic.DataModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by km on 28.11.2015.
 */
public class VolumeResultFlags {

    @JsonProperty("has_flowing_text") private boolean hasFlowingText;
    @JsonProperty("has_scanned_text") private boolean hasScannedText;
    @JsonProperty("can_download_pdf") private boolean canDownloadPdf;
    @JsonProperty("can_download_epub") private boolean canDownloadEpub;
    @JsonProperty("is_pdf_drm_enabled") private boolean pdfDrmEnabled;
    @JsonProperty("is_epub_drm_enabled") private boolean epubDrmEnabled;

    public boolean isHasFlowingText() {
        return hasFlowingText;
    }

    public void setHasFlowingText(boolean hasFlowingText) {
        this.hasFlowingText = hasFlowingText;
    }

    public boolean isHasScannedText() {
        return hasScannedText;
    }

    public void setHasScannedText(boolean hasScannedText) {
        this.hasScannedText = hasScannedText;
    }

    public boolean isCanDownloadPdf() {
        return canDownloadPdf;
    }

    public void setCanDownloadPdf(boolean canDownloadPdf) {
        this.canDownloadPdf = canDownloadPdf;
    }

    public boolean isCanDownloadEpub() {
        return canDownloadEpub;
    }

    public void setCanDownloadEpub(boolean canDownloadEpub) {
        this.canDownloadEpub = canDownloadEpub;
    }

    public boolean isPdfDrmEnabled() {
        return pdfDrmEnabled;
    }

    public void setPdfDrmEnabled(boolean pdfDrmEnabled) {
        this.pdfDrmEnabled = pdfDrmEnabled;
    }

    public boolean isEpubDrmEnabled() {
        return epubDrmEnabled;
    }

    public void setEpubDrmEnabled(boolean epubDrmEnabled) {
        this.epubDrmEnabled = epubDrmEnabled;
    }
}
