package ru.kmorozov.gbd.core.logic.model.book.google;

import com.google.gson.annotations.SerializedName;
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData;

import java.io.Serializable;
import java.net.URI;

import static ru.kmorozov.gbd.core.logic.extractors.google.GoogleImageExtractor.BOOK_ID_PLACEHOLDER;
import static ru.kmorozov.gbd.core.logic.extractors.google.GoogleImageExtractor.HTTPS_TEMPLATE;

/**
 * Created by km on 28.11.2015.
 */
public class GoogleBookData implements IBookData, Serializable {

    @SerializedName("fullview")
    private boolean fullview;
    @SerializedName("page_width")
    private int pageWidth;
    @SerializedName("page_height")
    private int pageHeight;
    @SerializedName("font_height")
    private int fontHeight;
    @SerializedName("first_content_page")
    private int firstContentPage;
    @SerializedName("disable_twopage")
    private boolean disableTwopage;
    @SerializedName("initial_zoom_width_override")
    private int initialZoomWidthOverride;
    @SerializedName("show_print_pages_button")
    private boolean showPrintPagesButton;
    @SerializedName("title")
    private String title;
    @SerializedName("subtitle")
    private String subtitle;
    @SerializedName("attribution")
    private String attribution;
    @SerializedName("additional_info")
    private AdditionalInfo additionalInfo;
    @SerializedName("table_of_contents_page_id")
    private String tableOfContentsPageId;
    @SerializedName("max_resolution_image_width")
    private int maxResolutionImageWidth;
    @SerializedName("max_resolution_image_height")
    private int maxResolutionImageHeight;
    @SerializedName("num_toc_pages")
    private int numTocPages;
    @SerializedName("quality_info")
    private String qualityInfo;
    @SerializedName("volume_id")
    private String volumeId;
    @SerializedName("permission_info")
    private String permissionInfo;
    @SerializedName("is_ebook")
    private boolean ebook;
    @SerializedName("volumeresult")
    private VolumeResultFlags volumeresult;
    @SerializedName("rating")
    private int rating;
    @SerializedName("num_reviews")
    private int numReviews;
    @SerializedName("publisher")
    private String publisher;
    @SerializedName("publication_date")
    private String publicationDate;
    @SerializedName("num_pages")
    private int numPages;
    @SerializedName("sample_url")
    private String sampleUrl;
    @SerializedName("synposis")
    private String synposis;
    @SerializedName("my_library_url")
    private String myLibraryUrl;
    @SerializedName("is_magazine")
    private boolean magazine;
    @SerializedName("is_public_domain")
    private boolean publicDomain;
    @SerializedName("last_page")
    private GooglePageInfo lastPage;

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getVolumeId() {
        return volumeId;
    }

    public VolumeResultFlags getFlags() {
        return volumeresult;
    }

    public URI getBaseUrl() {
        return URI.create(HTTPS_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, volumeId));
    }
}
