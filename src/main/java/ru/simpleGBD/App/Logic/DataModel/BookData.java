package ru.simpleGBD.App.Logic.DataModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by km on 28.11.2015.
 */
public class BookData {

    private boolean fullview;
    @JsonProperty("page_width") private int pageWidth;
    @JsonProperty("page_height") private int pageHeight;
    @JsonProperty("font_height") private int fontHeight;
    @JsonProperty("first_content_page") private int firstContentPage;
    @JsonProperty("disable_twopage") private boolean disableTwopage;
    @JsonProperty("initial_zoom_width_override") private int initialZoomWidthOverride;
    @JsonProperty("show_print_pages_button") private boolean showPrintPagesButton;
    private String title, subtitle, attribution;
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
    private int rating;
    @JsonProperty("num_reviews") private int numReviews;
    @JsonProperty("publisher") private String publisher;
    @JsonProperty("publication_date") private String publicationDate;
    @JsonProperty("num_pages") private int numPages;
    @JsonProperty("sample_url") private String sampleUrl;
    @JsonProperty("my_library_url") private String myLibraryUrl;
    @JsonProperty("is_magazine") private boolean magazine;
    @JsonProperty("is_public_domain") private boolean publicDomain;

    public boolean isFullview() {
        return fullview;
    }

    public void setFullview(boolean fullview) {
        this.fullview = fullview;
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public void setPageWidth(int pageWidth) {
        this.pageWidth = pageWidth;
    }

    public int getPageHeight() {
        return pageHeight;
    }

    public void setPageHeight(int pageHeight) {
        this.pageHeight = pageHeight;
    }

    public int getFontHeight() {
        return fontHeight;
    }

    public void setFontHeight(int fontHeight) {
        this.fontHeight = fontHeight;
    }

    public int getFirstContentPage() {
        return firstContentPage;
    }

    public void setFirstContentPage(int firstContentPage) {
        this.firstContentPage = firstContentPage;
    }

    public boolean isDisableTwopage() {
        return disableTwopage;
    }

    public void setDisableTwopage(boolean disableTwopage) {
        this.disableTwopage = disableTwopage;
    }

    public int getInitialZoomWidthOverride() {
        return initialZoomWidthOverride;
    }

    public void setInitialZoomWidthOverride(int initialZoomWidthOverride) {
        this.initialZoomWidthOverride = initialZoomWidthOverride;
    }

    public boolean isShowPrintPagesButton() {
        return showPrintPagesButton;
    }

    public void setShowPrintPagesButton(boolean showPrintPagesButton) {
        this.showPrintPagesButton = showPrintPagesButton;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public AdditionalInfo getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(AdditionalInfo additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getTableOfContentsPageId() {
        return tableOfContentsPageId;
    }

    public void setTableOfContentsPageId(String tableOfContentsPageId) {
        this.tableOfContentsPageId = tableOfContentsPageId;
    }

    public int getMaxResolutionImageWidth() {
        return maxResolutionImageWidth;
    }

    public void setMaxResolutionImageWidth(int maxResolutionImageWidth) {
        this.maxResolutionImageWidth = maxResolutionImageWidth;
    }

    public int getMaxResolutionImageHeight() {
        return maxResolutionImageHeight;
    }

    public void setMaxResolutionImageHeight(int maxResolutionImageHeight) {
        this.maxResolutionImageHeight = maxResolutionImageHeight;
    }

    public int getNumTocPages() {
        return numTocPages;
    }

    public void setNumTocPages(int numTocPages) {
        this.numTocPages = numTocPages;
    }

    public String getQualityInfo() {
        return qualityInfo;
    }

    public void setQualityInfo(String qualityInfo) {
        this.qualityInfo = qualityInfo;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    public String getPermissionInfo() {
        return permissionInfo;
    }

    public void setPermissionInfo(String permissionInfo) {
        this.permissionInfo = permissionInfo;
    }

    public boolean isEbook() {
        return ebook;
    }

    public void setEbook(boolean ebook) {
        this.ebook = ebook;
    }

    public VolumeResultFlags getVolumeresult() {
        return volumeresult;
    }

    public void setVolumeresult(VolumeResultFlags volumeresult) {
        this.volumeresult = volumeresult;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getNumReviews() {
        return numReviews;
    }

    public void setNumReviews(int numReviews) {
        this.numReviews = numReviews;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public int getNumPages() {
        return numPages;
    }

    public void setNumPages(int numPages) {
        this.numPages = numPages;
    }

    public String getSampleUrl() {
        return sampleUrl;
    }

    public void setSampleUrl(String sampleUrl) {
        this.sampleUrl = sampleUrl;
    }

    public String getMyLibraryUrl() {
        return myLibraryUrl;
    }

    public void setMyLibraryUrl(String myLibraryUrl) {
        this.myLibraryUrl = myLibraryUrl;
    }

    public boolean isMagazine() {
        return magazine;
    }

    public void setMagazine(boolean magazine) {
        this.magazine = magazine;
    }

    public boolean isPublicDomain() {
        return publicDomain;
    }

    public void setPublicDomain(boolean publicDomain) {
        this.publicDomain = publicDomain;
    }
}
