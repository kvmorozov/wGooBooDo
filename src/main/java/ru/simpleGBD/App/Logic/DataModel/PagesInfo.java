package ru.simpleGBD.App.Logic.DataModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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

    @JsonProperty("page")
    private PageInfo[] pages;
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

    public void build() {
        List<PageInfo> _pages = Arrays.asList(pages);
        pagesMap = new ConcurrentHashMap<>(_pages.size());
        pagesList = new LinkedList<>();
        for (PageInfo page : _pages) {
            pagesMap.put(page.getPid(), page);
            pagesList.add(page);
        }
    }

    public PageInfo getPageByPid(String pid) {
        return pagesMap.get(pid);
    }

    public LinkedList<PageInfo> getPages() {
        return pagesList;
    }

    public void exportPagesUrls() throws IOException {
        StringBuffer imgUrlsBuffer = new StringBuffer();
        BufferedWriter bwr = new BufferedWriter(new FileWriter(new File(ExecutionContext.outputDir.getPath() + "\\" + "urls.txt")));

        int foundPagesCount = 0;

        for (PageInfo page : pagesMap.values())
            if (page.getSig() != null) {
                foundPagesCount++;
                imgUrlsBuffer.append(page.getPageUrl()).append(System.getProperty("line.separator"));
            }

        bwr.write(imgUrlsBuffer.toString());
        bwr.flush();
        bwr.close();

        logger.info(String.format("Found %d sigs!", foundPagesCount));
    }

    public String getMissingPagesList() {
        StringBuilder bList = new StringBuilder();
        List<Pair<PageInfo, PageInfo>> pairs = new ArrayList<>();

        PageInfo blockStart = null, prevPage = null, currentPage;

        ListIterator<PageInfo> itr = pagesList.listIterator();
        while (itr.hasNext()) {
            currentPage = itr.next();

            if (currentPage.dataProcessed.get())
                if (blockStart == null) {
                } else {
                    pairs.add(new ImmutablePair(blockStart, prevPage));
                    blockStart = null;
                }
            else if (blockStart == null)
                blockStart = currentPage;
            else {
            }

            if (!itr.hasNext() && blockStart != null)
                pairs.add(new ImmutablePair(blockStart, currentPage));

            prevPage = currentPage;
        }

        for (Pair<PageInfo, PageInfo> pair : pairs)
            bList.append(String.format("%s-%s, ", pair.getLeft().getPid(), pair.getRight().getPid()));

        bList.deleteCharAt(bList.length() - 1);
        bList.deleteCharAt(bList.length() - 1);

        return bList.toString();
    }
}
