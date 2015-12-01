package ru.simpleGBD.App.Logic.DataModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.simpleGBD.App.Logic.ExecutionContext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Created by km on 21.11.2015.
 */
public class PagesInfo {

    private static Logger logger = Logger.getLogger(PagesInfo.class.getName());

    @JsonProperty("page") private PageInfo[] pages;
    private Map<String, PageInfo> pagesMap;
    private String prefix;

    public PageInfo[] getPagesArray() {
        return pages;
    }

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
        List<PageInfo> _pages = Arrays.asList(getPagesArray());
        pagesMap = new ConcurrentHashMap<>(_pages.size());
        for(PageInfo page : _pages)
            pagesMap.put(page.getPid(), page);
    }

    public PageInfo getPageByPid(String pid) {return pagesMap.get(pid);}
    public Collection<PageInfo> getPages() {return pagesMap.values();}

    public void exportPagesUrls() throws IOException {
        StringBuffer imgUrlsBuffer = new StringBuffer();
        BufferedWriter bwr = new BufferedWriter(new FileWriter(new File(ExecutionContext.outputDir.getPath() + "\\" + "urls.txt")));

        int foundPagesCount = 0;

        for(PageInfo page : pagesMap.values())
            if (page.getSig() != null) {
                foundPagesCount++;
                imgUrlsBuffer.append(page.getPageUrl()).append(System.getProperty("line.separator"));
            }

        bwr.write(imgUrlsBuffer.toString());
        bwr.flush();
        bwr.close();

        logger.info(String.format("Found %d sigs!", foundPagesCount));
    }
}
