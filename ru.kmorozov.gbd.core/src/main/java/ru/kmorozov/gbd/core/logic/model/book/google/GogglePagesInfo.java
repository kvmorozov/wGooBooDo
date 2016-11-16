package ru.kmorozov.gbd.core.logic.model.book.google;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo;
import ru.kmorozov.gbd.core.utils.Logger;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static ru.kmorozov.gbd.core.logic.context.ExecutionContext.INSTANCE;

/**
 * Created by km on 21.11.2015.
 */
public class GogglePagesInfo implements IPagesInfo, Serializable {

    private static final Logger logger = INSTANCE.getLogger(GogglePagesInfo.class);

    @JsonProperty("page")
    private GogglePageInfo[] pages;
    @JsonProperty("prefix")
    private String prefix;

    private Map<String, GogglePageInfo> pagesMap;
    private LinkedList<GogglePageInfo> pagesList;

    private void addPage(GogglePageInfo page) {
        pagesMap.put(page.getPid(), page);
        pagesList.add(page);
    }

    @Override
    public void build() {
        List<GogglePageInfo> _pages = Arrays.asList(pages);
        pagesMap = new ConcurrentHashMap<>(_pages.size());
        pagesList = new LinkedList<>();

        _pages.sort(GogglePageInfo::compareTo);

        GogglePageInfo prevPage = null;
        for (GogglePageInfo page : _pages) {
            addPage(page);

            if (prevPage != null && page.getOrder() - prevPage.getOrder() > 1) fillGap(prevPage, page);

            prevPage = page;
        }
    }

    private void fillGap(GogglePageInfo beginGap, GogglePageInfo endGap) {
        String beginPagePrefix = beginGap.getPrefix();
        String endPagePrefix = endGap.getPrefix();

        int beginPageNum = beginGap.getPageNum();
        int endPageNum = endGap.getPageNum();

        if (beginPageNum >= endPageNum && endPageNum > 1 && beginPagePrefix.equals(endPagePrefix))
            logger.severe(String.format("Cannot fill gap between pages %s(order=%s) and %s(order=%s)", beginGap.getPid(), beginGap.getOrder(), endGap.getPid(), endGap.getOrder()));

        if (beginPagePrefix.equals(endPagePrefix))
            for (int index = beginGap.getOrder() + 1; index < endGap.getOrder(); index++) {
                String pid = beginPageNum > 0 ? beginPagePrefix + (beginPageNum + index - beginGap.getOrder()) : beginGap.getPid() + "_" + index;
                GogglePageInfo gapPage = new GogglePageInfo(pid, index);
                addPage(gapPage);
            }

        if (beginPageNum > 0 && endPageNum > 0)
            for (int index = beginGap.getOrder() + 1; index < endGap.getOrder() - endPageNum; index++) {
                GogglePageInfo gapPage = new GogglePageInfo(beginPagePrefix + String.valueOf(index + 1), index);
                addPage(gapPage);
            }
        else {
            if (endPageNum > 1) {
                int pagesToCreate = endGap.getOrder() - beginGap.getOrder() - 1;
                int pagesCreated = 0;
                for (int index = 1; index <= pagesToCreate; index++) {
                    if (endPageNum - index < 1) break;
                    String newPagePidFromEnd = endPagePrefix + (endPageNum - index);
                    if (!pagesMap.containsKey(newPagePidFromEnd) && !beginPagePrefix.contains(newPagePidFromEnd)) {
                        GogglePageInfo gapPage = new GogglePageInfo(newPagePidFromEnd, endGap.getOrder() - index);
                        addPage(gapPage);
                        pagesCreated++;
                    } else break;
                }
                if (pagesCreated < pagesToCreate) {
                    pagesToCreate = pagesToCreate - pagesCreated;
                    for (int index = 1; index <= pagesToCreate; index++) {
                        String newPagePidFromBegin = beginPagePrefix + (beginPageNum + index);
                        GogglePageInfo gapPage = new GogglePageInfo(newPagePidFromBegin, beginGap.getOrder() + index);
                        addPage(gapPage);
                    }
                }
            } else if (beginPageNum > 1 && endPageNum < 0) {
                logger.severe(String.format("Cannot fill gap between pages %s(order=%s) and %s(order=%s)", beginGap.getPid(), beginGap.getOrder(), endGap.getPid(), endGap.getOrder()));
            }
        }

        pagesList.sort(GogglePageInfo::compareTo);
    }

    @Override
    public GogglePageInfo getPageByPid(String pid) {
        return pagesMap.get(pid);
    }

    @Override
    public GogglePageInfo[] getPages() {
        return pagesMap.values().toArray(new GogglePageInfo[pagesMap.size()]);
    }

    public void setPages(GogglePageInfo[] pages) {
        this.pages = pages;
        this.pagesMap = new HashMap<>();

        if (pages != null) for (GogglePageInfo page : pages)
            pagesMap.put(page.getPid(), page);
    }

    @JsonIgnore
    @Override
    public String getMissingPagesList() {
        Predicate<GogglePageInfo> predicate = pageInfo -> !pageInfo.fileExists.get();
        return getListByCondition(predicate);
    }

    private Pair<GogglePageInfo, GogglePageInfo> createPair(GogglePageInfo p1, GogglePageInfo p2) {
        return p1.getOrder() < p2.getOrder() ? new ImmutablePair(p1, p2) : new ImmutablePair(p2, p1);
    }

    private String getListByCondition(Predicate<GogglePageInfo> condition) {
        StringBuilder bList = new StringBuilder();
        List<Pair<GogglePageInfo, GogglePageInfo>> pairs = new ArrayList<>();

        GogglePageInfo blockStart = null, prevPage = null;

        long filteredCount = pagesList.stream().filter(condition).count();
        GogglePageInfo lastPage = pagesList.getLast();

        for (GogglePageInfo currentPage : pagesList) {
            if (condition.test(currentPage)) if (blockStart == null) {
                blockStart = currentPage;
            } else {
            }
            else {
                if (blockStart == null) {
                } else {
                    pairs.add(createPair(blockStart, prevPage));
                    blockStart = null;
                }
            }

            if (currentPage.equals(lastPage) && blockStart != null) pairs.add(createPair(blockStart, currentPage));

            prevPage = currentPage;
        }

        for (Pair<GogglePageInfo, GogglePageInfo> pair : pairs)
            if (pair.getLeft() == pair.getRight()) bList.append(String.format("%s, ", pair.getLeft().getPid()));
            else bList.append(String.format("%s-%s, ", pair.getLeft().getPid(), pair.getRight().getPid()));

        if (bList.length() > 0) {
            bList.deleteCharAt(bList.length() - 1).deleteCharAt(bList.length() - 1);
            bList.append(String.format(". Total = %d/%d", filteredCount, pagesList.size()));
        }

        return bList.toString();
    }
}
