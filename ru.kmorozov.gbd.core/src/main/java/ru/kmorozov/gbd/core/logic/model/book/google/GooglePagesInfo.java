package ru.kmorozov.gbd.core.logic.model.book.google;

import com.google.gson.annotations.SerializedName;
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
public class GooglePagesInfo implements IPagesInfo, Serializable {

    private static final Logger logger = INSTANCE.getLogger(GooglePagesInfo.class);

    @SerializedName("page")
    private GooglePageInfo[] pages;
    @SerializedName("prefix")
    private String prefix;

    private transient Map<String, GooglePageInfo> pagesMap;
    private transient LinkedList<GooglePageInfo> pagesList;

    private void addPage(GooglePageInfo page) {
        pagesMap.put(page.getPid(), page);
        pagesList.add(page);
    }

    @Override
    public void build() {
        List<GooglePageInfo> _pages = Arrays.asList(pages);
        pagesMap = new ConcurrentHashMap<>(_pages.size());
        pagesList = new LinkedList<>();

        _pages.sort(GooglePageInfo::compareTo);

        GooglePageInfo prevPage = null;
        for (GooglePageInfo page : _pages) {
            addPage(page);

            if (prevPage != null && page.getOrder() - prevPage.getOrder() > 1) fillGap(prevPage, page);

            prevPage = page;
        }

        pages = pagesMap.values().toArray(new GooglePageInfo[pagesMap.size()]);
    }

    private void fillGap(GooglePageInfo beginGap, GooglePageInfo endGap) {
        if (beginGap.isGapPage() || endGap.isGapPage())
            return;

        String beginPagePrefix = beginGap.getPrefix();
        String endPagePrefix = endGap.getPrefix();

        int beginPageNum = beginGap.getPageNum();
        int endPageNum = endGap.getPageNum();

        if (beginPageNum >= endPageNum && endPageNum > 1 && beginPagePrefix.equals(endPagePrefix))
            logger.severe(String.format("Cannot fill gap between pages %s(order=%s) and %s(order=%s)", beginGap.getPid(), beginGap.getOrder(), endGap.getPid(), endGap.getOrder()));

        if (beginPagePrefix.equals(endPagePrefix)) {
            for (int index = beginGap.getOrder() + 1; index < endGap.getOrder(); index++) {
                String pid = beginPageNum > 0 ? beginPagePrefix + (beginPageNum + index - beginGap.getOrder()) : beginGap.getPid() + "_" + index;
                GooglePageInfo gapPage = new GooglePageInfo(pid, index);
                addPage(gapPage);
            }

            if (beginPageNum > 0 && endPageNum > 0)
                for (int index = beginGap.getOrder() + 1; index < endGap.getOrder() - endPageNum; index++) {
                    GooglePageInfo gapPage = new GooglePageInfo(beginPagePrefix + String.valueOf(index + 1), index);
                    addPage(gapPage);
                }
        } else {
            if (endPageNum >= 1) {
                int pagesToCreate = endGap.getOrder() - beginGap.getOrder() - 1;
                int pagesCreated = 0;
                for (int index = 1; index <= pagesToCreate; index++) {
                    if (endPageNum - index < 1) break;
                    String newPagePidFromEnd = endPagePrefix + (endPageNum - index);
                    if (!pagesMap.containsKey(newPagePidFromEnd) && !beginPagePrefix.contains(newPagePidFromEnd)) {
                        GooglePageInfo gapPage = new GooglePageInfo(newPagePidFromEnd, endGap.getOrder() - index);
                        addPage(gapPage);
                        pagesCreated++;
                    } else break;
                }
                if (pagesCreated < pagesToCreate) {
                    pagesToCreate = pagesToCreate - pagesCreated;
                    for (int index = 1; index <= pagesToCreate; index++) {
                        String newPagePidFromBegin = beginPagePrefix + (beginPageNum + index);
                        GooglePageInfo gapPage = new GooglePageInfo(newPagePidFromBegin, beginGap.getOrder() + index);
                        addPage(gapPage);
                    }
                }
            } else if (beginPageNum > 1 && endPageNum < 0) {
                logger.severe(String.format("Cannot fill gap between pages %s(order=%s) and %s(order=%s)", beginGap.getPid(), beginGap.getOrder(), endGap.getPid(), endGap.getOrder()));
            }
        }

        pagesList.sort(GooglePageInfo::compareTo);
    }

    @Override
    public GooglePageInfo getPageByPid(String pid) {
        return pagesMap.get(pid);
    }

    @Override
    public GooglePageInfo[] getPages() {
        return pages;
    }

    public void setPages(GooglePageInfo[] pages) {
        this.pages = pages;
        this.pagesMap = new HashMap<>();

        if (pages != null) for (GooglePageInfo page : pages)
            pagesMap.put(page.getPid(), page);
    }

    @Override
    public String getMissingPagesList() {
        Predicate<GooglePageInfo> predicate = pageInfo -> !pageInfo.fileExists.get();
        return getListByCondition(predicate);
    }

    private Pair<GooglePageInfo, GooglePageInfo> createPair(GooglePageInfo p1, GooglePageInfo p2) {
        return p1.getOrder() < p2.getOrder() ? new ImmutablePair<>(p1, p2) : new ImmutablePair<>(p2, p1);
    }

    private String getListByCondition(Predicate<GooglePageInfo> condition) {
        StringBuilder bList = new StringBuilder();
        List<Pair<GooglePageInfo, GooglePageInfo>> pairs = new ArrayList<>();

        GooglePageInfo blockStart = null, prevPage = null;

        long filteredCount = pagesList.stream().filter(condition).count();
        GooglePageInfo lastPage = pagesList.getLast();

        for (GooglePageInfo currentPage : pagesList) {
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

        for (Pair<GooglePageInfo, GooglePageInfo> pair : pairs)
            if (pair.getLeft() == pair.getRight()) bList.append(String.format("%s, ", pair.getLeft().getPid()));
            else bList.append(String.format("%s-%s, ", pair.getLeft().getPid(), pair.getRight().getPid()));

        if (bList.length() > 0) {
            bList.deleteCharAt(bList.length() - 1).deleteCharAt(bList.length() - 1);
            bList.append(String.format(". Total = %d/%d", filteredCount, pagesList.size()));
        }

        return bList.toString();
    }
}
