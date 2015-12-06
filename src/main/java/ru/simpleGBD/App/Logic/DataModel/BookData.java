package ru.simpleGBD.App.Logic.DataModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by km on 28.11.2015.
 */
public class BookData {

    @JsonProperty("fullview") private boolean fullview;
    @JsonProperty("page_width") private int pageWidth;
    @JsonProperty("page_height") private int pageHeight;
    @JsonProperty("font_height") private int fontHeight;
    @JsonProperty("first_content_page") private int firstContentPage;
    @JsonProperty("disable_twopage") private boolean disableTwopage;
    @JsonProperty("initial_zoom_width_override") private int initialZoomWidthOverride;
    @JsonProperty("show_print_pages_button") private boolean showPrintPagesButton;
    @JsonProperty("title") private String title;
    @JsonProperty("subtitle") private String subtitle;
    @JsonProperty("attribution") private String attribution;
    @JsonProperty("additional_info") private AdditionalInfo additionalInfo;
    @JsonProperty("table_of_contents_page_id") private String tableOfContentsPageId;
    @JsonProperty("max_resolution_image_width") private int maxResolutionImageWidth;
    @JsonProperty("max_resolution_image_height") private int maxResolutionImageHeight;
    @JsonProperty("num_toc_pages") private int numTocPages;
    @JsonProperty("quality_info") private String qualityInfo;
    @JsonProperty("volume_id") private String volumeId;
    @JsonProperty("permission_info") private String permissionInfo;
    @JsonProperty("is_ebook") private boolean ebook;
    @JsonProperty("volumeresult") private VolumeResultFlags volumeresult;
    @JsonProperty("rating") private int rating;
    @JsonProperty("num_reviews") private int numReviews;
    @JsonProperty("publisher") private String publisher;
    @JsonProperty("publication_date") private String publicationDate;
    @JsonProperty("num_pages") private int numPages;
    @JsonProperty("sample_url") private String sampleUrl;
    @JsonProperty("synposis") private String synposis;
    @JsonProperty("my_library_url") private String myLibraryUrl;
    @JsonProperty("is_magazine") private boolean magazine;
    @JsonProperty("is_public_domain") private boolean publicDomain;

    public String getTitle() {
        return title;
    }

    public String getVolumeId() {
        return volumeId;
    }
}