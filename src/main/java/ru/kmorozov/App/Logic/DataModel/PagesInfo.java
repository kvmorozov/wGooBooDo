package ru.kmorozov.App.Logic.DataModel;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by km on 21.11.2015.
 */
public class PagesInfo {

    private PageInfo[] pages;
    private Map<String, PageInfo> pagesMap;
    private String prefix;

    public PageInfo[] getPages() {
        return pages;
    }

    @JsonProperty("page")
    public void setPages(PageInfo[] pages) {
        this.pages = pages;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getPagesCount() {return pages.length;}

    public void build() {
        List<PageInfo> _pages = Arrays.asList(getPages());
        pagesMap = new HashMap<String, PageInfo>(_pages.size());
        for(PageInfo page : _pages)
            pagesMap.put(page.getPid(), page);
    }

    public PageInfo getPageByPid(String pid) {return pagesMap.get(pid);}
}
