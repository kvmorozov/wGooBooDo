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
}
