package ru.kmorozov.db.core.logic.model.book.google;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.gbd.logger.output.DummyReceiver;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Created by km on 21.11.2015.
 */
public class GooglePagesInfo implements IPagesInfo, Serializable {

    @SerializedName("page")
    private GooglePageInfo[] pages;
    @SerializedName("prefix")
    private String prefix;

    private transient Map<String, GooglePageInfo> pagesMap;
    private transient LinkedList<GooglePageInfo> pagesList;

    private void addPage(GooglePageInfo page) {
        this.pagesMap.put(page.getPid(), page);
        this.pagesList.add(page);
    }

    @Override
    public void build() {
        List<GooglePageInfo> _pages = Arrays.asList(this.pages);
        this.pagesMap = new ConcurrentHashMap<>(_pages.size());
        this.pagesList = new LinkedList<>();

        _pages.sort(GooglePageInfo::compareTo);

        GooglePageInfo prevPage = null;
        for (GooglePageInfo page : _pages) {
            this.addPage(page);

            if (null != prevPage && 1 < page.getOrder() - prevPage.getOrder()) this.fillGap(prevPage, page);

            prevPage = page;
        }

        this.pages = this.pagesMap.values().toArray(new GooglePageInfo[0]);
    }

    private void fillGap(GooglePageInfo beginGap, GooglePageInfo endGap) {
        final Logger logger = new Logger(new DummyReceiver(), "gapFinder", ": ");

        if (beginGap.isGapPage() || endGap.isGapPage()) return;

        String beginPagePrefix = beginGap.getPrefix();
        String endPagePrefix = endGap.getPrefix();

        int beginPageNum = beginGap.getPageNum();
        int endPageNum = endGap.getPageNum();

        if (beginPageNum >= endPageNum && 1 < endPageNum && beginPagePrefix.equals(endPagePrefix))
            logger.severe(String.format("Cannot fill gap between pages %s(order=%s) and %s(order=%s)", beginGap.getPid(), beginGap.getOrder(), endGap.getPid(), endGap.getOrder()));

        if (beginPagePrefix.equals(endPagePrefix)) {
            for (int index = beginGap.getOrder() + 1; index < endGap.getOrder(); index++) {
                String pid = 0 < beginPageNum ? beginPagePrefix + (beginPageNum + index - beginGap.getOrder()) : beginGap.getPid() + '_' + index;
                GooglePageInfo gapPage = new GooglePageInfo(pid, index);
                this.addPage(gapPage);
            }

            if (0 < beginPageNum && 0 < endPageNum)
                for (int index = beginGap.getOrder() + 1; index < endGap.getOrder() - endPageNum; index++) {
                    GooglePageInfo gapPage = new GooglePageInfo(beginPagePrefix + (index + 1), index);
                    this.addPage(gapPage);
                }
        } else {
            if (1 <= endPageNum) {
                int pagesToCreate = endGap.getOrder() - beginGap.getOrder() - 1;
                int pagesCreated = 0;
                for (int index = 1; index <= pagesToCreate; index++) {
                    if (1 > endPageNum - index) break;
                    String newPagePidFromEnd = endPagePrefix + (endPageNum - index);
                    if (!this.pagesMap.containsKey(newPagePidFromEnd) && !beginPagePrefix.contains(newPagePidFromEnd)) {
                        GooglePageInfo gapPage = new GooglePageInfo(newPagePidFromEnd, endGap.getOrder() - index);
                        this.addPage(gapPage);
                        pagesCreated++;
                    } else break;
                }
                if (pagesCreated < pagesToCreate) {
                    pagesToCreate -= pagesCreated;
                    for (int index = 1; index <= pagesToCreate; index++) {
                        String newPagePidFromBegin = beginPagePrefix + (beginPageNum + index);
                        GooglePageInfo gapPage = new GooglePageInfo(newPagePidFromBegin, beginGap.getOrder() + index);
                        this.addPage(gapPage);
                    }
                }
            } else if (1 < beginPageNum && 0 > endPageNum) {
                logger.severe(String.format("Cannot fill gap between pages %s(order=%s) and %s(order=%s)", beginGap.getPid(), beginGap.getOrder(), endGap.getPid(), endGap.getOrder()));
            }
        }

        this.pagesList.sort(GooglePageInfo::compareTo);
    }

    @Override
    public GooglePageInfo getPageByPid(String pid) {
        return this.pagesMap.get(pid);
    }

    @Override
    public GooglePageInfo[] getPages() {
        return this.pages;
    }

    public void setPages(GooglePageInfo[] pages) {
        this.pages = pages;
        pagesMap = new HashMap<>();

        if (null != pages) for (GooglePageInfo page : pages)
            this.pagesMap.put(page.getPid(), page);
    }

    @Override
    public String getMissingPagesList() {
        Predicate<GooglePageInfo> predicate = pageInfo -> !pageInfo.isFileExists();
        return this.getListByCondition(predicate);
    }

    private static Pair<GooglePageInfo, GooglePageInfo> createPair(GooglePageInfo p1, GooglePageInfo p2) {
        return p1.getOrder() < p2.getOrder() ? new ImmutablePair<>(p1, p2) : new ImmutablePair<>(p2, p1);
    }

    private String getListByCondition(Predicate<GooglePageInfo> condition) {
        StringBuilder bList = new StringBuilder();
        Collection<Pair<GooglePageInfo, GooglePageInfo>> pairs = new ArrayList<>();

        GooglePageInfo blockStart = null, prevPage = null;

        long filteredCount = this.pagesList.stream().filter(condition).count();
        GooglePageInfo lastPage = this.pagesList.getLast();

        for (GooglePageInfo currentPage : this.pagesList) {
            if (condition.test(currentPage)) if (null == blockStart) {
                blockStart = currentPage;
            } else {
            }
            else {
                if (null == blockStart) {
                } else {
                    pairs.add(GooglePagesInfo.createPair(blockStart, prevPage));
                    blockStart = null;
                }
            }

            if (currentPage.equals(lastPage) && null != blockStart) pairs.add(GooglePagesInfo.createPair(blockStart, currentPage));

            prevPage = currentPage;
        }

        for (Pair<GooglePageInfo, GooglePageInfo> pair : pairs)
            if (pair.getLeft() == pair.getRight()) bList.append(String.format("%s, ", pair.getLeft().getPid()));
            else bList.append(String.format("%s-%s, ", pair.getLeft().getPid(), pair.getRight().getPid()));

        if (0 < bList.length()) {
            bList.deleteCharAt(bList.length() - 1).deleteCharAt(bList.length() - 1);
            bList.append(String.format(". Total = %d/%d", filteredCount, this.pagesList.size()));
        }

        return bList.toString();
    }
}
