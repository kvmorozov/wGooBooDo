package ru.simpleGBD.App.Logic.model.book;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Utils.Logger;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Created by km on 21.11.2015.
 */
public class PagesInfo implements Serializable {

    private static final Logger logger = Logger.getLogger(ExecutionContext.output, PagesInfo.class.getName());

    @JsonProperty("page")
    private final PageInfo[] pages;
    @JsonProperty("prefix")
    private String prefix;

    private Map<String, PageInfo> pagesMap;
    private LinkedList<PageInfo> pagesList;

    public PageInfo[] getPagesArray() {
        return pages;
    }

    private void addPage(PageInfo page) {
        pagesMap.put(page.getPid(), page);
        pagesList.add(page);
    }

    public void build() {
        List<PageInfo> _pages = Arrays.asList(pages);
        pagesMap = new ConcurrentHashMap<>(_pages.size());
        pagesList = new LinkedList<>();

        PageInfo prevPage = null;
        for (PageInfo page : _pages) {
            addPage(page);

            if (prevPage != null && page.getOrder() - prevPage.getOrder() > 1 && GBDOptions.fillGaps())
                fillGap(prevPage, page);

            prevPage = page;
        }
    }

    private void fillGap(PageInfo beginGap, PageInfo endGap) {
        String beginPagePrefix = beginGap.getPrefix();
        String endPagePrefix = endGap.getPrefix();

        int beginPageNum = beginGap.getPageNum();
        int endPageNum = endGap.getPageNum();

        if (beginPageNum >= endPageNum && endPageNum > 1 && beginPagePrefix.equals(endPagePrefix))
            logger.severe(String.format("Cannot fill gap between pages %s(order=%s) and %s(order=%s)",
                    beginGap.getPid(), beginGap.getOrder(), endGap.getPid(), endGap.getOrder()));

        if (beginPagePrefix.equals(endPagePrefix))
            for (int index = beginGap.getOrder() + 1; index < endGap.getOrder(); index++) {
                String pid = beginPageNum > 0 ?
                        beginPagePrefix + (beginPageNum + index - beginGap.getOrder()) :
                        beginGap.getPid() + "_" + index;
                PageInfo gapPage = new PageInfo(pid, index);
                addPage(gapPage);
            }
        else {
            if (endPageNum > 1) {
                for (int index = endGap.getOrder() - 1; endPageNum + index - endGap.getOrder() > 0; index--) {
                    PageInfo gapPage = new PageInfo(endPagePrefix + (endPageNum + index - endGap.getOrder()), index);
                    addPage(gapPage);
                }
            } else if (beginPageNum > 1 && endPageNum < 0) {
                logger.severe(String.format("Cannot fill gap between pages %s(order=%s) and %s(order=%s)",
                        beginGap.getPid(), beginGap.getOrder(), endGap.getPid(), endGap.getOrder()));
            }

            if (beginPageNum > 0 && endPageNum > 0)
                for (int index = beginGap.getOrder() + 1; index < endGap.getOrder() - endPageNum; index++) {
                    PageInfo gapPage = new PageInfo(beginPagePrefix + String.valueOf(index + 1), index);
                    addPage(gapPage);
                }
        }

        pagesList.sort((o1, o2) -> o1.getOrder().compareTo(o2.getOrder()));
    }

    public PageInfo getPageByPid(String pid) {
        return pagesMap.get(pid);
    }

    public Collection<PageInfo> getPages() {
        return pagesMap.values();
    }

    public String getMissingPagesList() {
        return getListByCondition(pageInfo -> pageInfo.fileExists.get());
    }

    private Pair<PageInfo, PageInfo> createPair(PageInfo p1, PageInfo p2) {
        return p1.getOrder() < p2.getOrder() ? new ImmutablePair(p1, p2) : new ImmutablePair(p2, p1);
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
                    pairs.add(createPair(blockStart, prevPage));
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
                pairs.add(createPair(blockStart, currentPage));

            prevPage = currentPage;
        }

        for (Pair<PageInfo, PageInfo> pair : pairs)
            if (pair.getLeft() == pair.getRight())
                bList.append(String.format("%s, ", pair.getLeft().getPid()));
            else
                bList.append(String.format("%s-%s, ", pair.getLeft().getPid(), pair.getRight().getPid()));

        if (bList.length() > 0) {
            bList.deleteCharAt(bList.length() - 1).deleteCharAt(bList.length() - 1);
            bList.append(String.format(". Total = %d/%d", pagesCountByCondition, total));
        }

        return bList.toString();
    }
}
