package ru.simpleGBD.App.Logic.model.book;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Created by km on 21.11.2015.
 */
public class PagesInfo implements Serializable {

    @JsonProperty("page") private PageInfo[] pages;
    @JsonProperty("prefix") private String prefix;

    private Map<String, PageInfo> pagesMap;
    private LinkedList<PageInfo> pagesList;

    public PageInfo[] getPagesArray() {
        return pages;
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

    public String getMissingPagesList() {
        return getListByCondition(pageInfo -> pageInfo.fileExists.get());
    }

    private String getListByCondition(Predicate<PageInfo> condition) {
        StringBuilder bList = new StringBuilder();
        List<Pair<PageInfo, PageInfo>> pairs = new ArrayList<>();

        PageInfo blockStart = null, prevPage = null, currentPage;
        int pagesCountByCondition = 0, total = 0;

        ListIterator<PageInfo> itr = pagesList.listIterator();
        while (itr.hasNext()) {
            currentPage = itr.next();
            total++;

            if (condition.test(currentPage))
                if (blockStart == null) {
                } else {
                    pairs.add(new ImmutablePair(blockStart, prevPage));
                    blockStart = null;
                }
            else {
                pagesCountByCondition++;

                if (blockStart == null)
                    blockStart = currentPage;
                else {
                }
            }

            if (!itr.hasNext() && blockStart != null)
                pairs.add(new ImmutablePair(blockStart, currentPage));

            prevPage = currentPage;
        }

        for (Pair<PageInfo, PageInfo> pair : pairs)
            if (pair.getLeft() == pair.getRight())
                bList.append(String.format("%s, ", pair.getLeft().getPid()));
            else
                bList.append(String.format("%s-%s, ", pair.getLeft().getPid(), pair.getRight().getPid()));

        bList.deleteCharAt(bList.length() - 1).deleteCharAt(bList.length() - 1);
        bList.append(String.format(". Total = %d/%d", pagesCountByCondition, total));

        return bList.toString();
    }
}