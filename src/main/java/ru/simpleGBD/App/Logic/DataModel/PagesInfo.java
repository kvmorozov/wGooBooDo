package ru.simpleGBD.App.Logic.DataModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.simpleGBD.App.Logic.ExecutionContext;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Created by km on 21.11.2015.
 */
public class PagesInfo implements Serializable {

    private static Logger logger = Logger.getLogger(PagesInfo.class.getName());

    @JsonProperty("page") private PageInfo[] pages;
    private Map<String, PageInfo> pagesMap;
    private LinkedList<PageInfo> pagesList;
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
        List<PageInfo> _pages = Arrays.asList(pages);
        pagesMap = new ConcurrentHashMap<>(_pages.size());
        pagesList = new LinkedList<>();
        for(PageInfo page : _pages) {
            pagesMap.put(page.getPid(), page);
            pagesList.add(page);
        }
    }

    public PageInfo getPageByPid(String pid) {return pagesMap.get(pid);}
    public LinkedList<PageInfo> getPages() {return pagesList;}

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
