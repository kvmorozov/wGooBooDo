package ru.simpleGBD.App.Logic.DataModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by km on 28.11.2015.
 */
public class JsonBookInfo {

    @JsonProperty("BuyLinks") private BuyLink[] buyLinks;
    @JsonProperty("AboutUrl") private String aboutUrl;
    @JsonProperty("PreviewUrl") private String previewUrl;
    @JsonProperty("allowed_syndication_flags") private SyndicationFlags flags;
    @JsonProperty("online_access") private OnlineAccess onlineAccess;
    @JsonProperty("TocLine") private TocItem[] toc;

    public BuyLink[] getBuyLinks() {
        return buyLinks;
    }

    public void setBuyLinks(BuyLink[] buyLinks) {
        this.buyLinks = buyLinks;
    }

    public String getAboutUrl() {
        return aboutUrl;
    }

    public void setAboutUrl(String aboutUrl) {
        this.aboutUrl = aboutUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public SyndicationFlags getFlags() {
        return flags;
    }

    public void setFlags(SyndicationFlags flags) {
        this.flags = flags;
    }

    public TocItem[] getToc() {
        return toc;
    }

    public void setToc(TocItem[] toc) {
        this.toc = toc;
    }
}
