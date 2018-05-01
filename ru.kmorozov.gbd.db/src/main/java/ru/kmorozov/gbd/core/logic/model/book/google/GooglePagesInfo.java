package ru.kmorozov.gbd.core.logic.model.book.google;

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

    private void addPage(final GooglePageInfo page) {
        pagesMap.put(page.getPid(), page);
        pagesList.add(page);
    }

    @Override
    public void build() {
        final List<GooglePageInfo> _pages = Arrays.asList(pages);
        pagesMap = new ConcurrentHashMap<>(_pages.size());
        pagesList = new LinkedList<>();

        _pages.sort(GooglePageInfo::compareTo);

        GooglePageInfo prevPage = null;
        for (final GooglePageInfo page : _pages) {
            addPage(page);

            if (null != prevPage && 1 < page.getOrder() - prevPage.getOrder()) fillGap(prevPage, page);

            prevPage = page;
        }

        pages = pagesMap.values().toArray(new GooglePageInfo[pagesMap.size()]);
    }

    private void fillGap(final GooglePageInfo beginGap, final GooglePageInfo endGap) {
        Logger logger = new Logger(new DummyReceiver(), "gapFinder", ": ");;

        if (beginGap.isGapPage() || endGap.isGapPage()) return;

        final String beginPagePrefix = beginGap.getPrefix();
        final String endPagePrefix = endGap.getPrefix();

        final int beginPageNum = beginGap.getPageNum();
        final int endPageNum = endGap.getPageNum();

        if (beginPageNum >= endPageNum && 1 < endPageNum && beginPagePrefix.equals(endPagePrefix))
            logger.severe(String.format("Cannot fill gap between pages %s(order=%s) and %s(order=%s)", beginGap.getPid(), beginGap.getOrder(), endGap.getPid(), endGap.getOrder()));

        if (beginPagePrefix.equals(endPagePrefix)) {
            for (int index = beginGap.getOrder() + 1; index < endGap.getOrder(); index++) {
                final String pid = 0 < beginPageNum ? beginPagePrefix + (beginPageNum + index - beginGap.getOrder()) : beginGap.getPid() + '_' + index;
                final GooglePageInfo gapPage = new GooglePageInfo(pid, index);
                addPage(gapPage);
            }

            if (0 < beginPageNum && 0 < endPageNum)
                for (int index = beginGap.getOrder() + 1; index < endGap.getOrder() - endPageNum; index++) {
                    final GooglePageInfo gapPage = new GooglePageInfo(beginPagePrefix + (index + 1), index);
                    addPage(gapPage);
                }
        } else {
            if (1 <= endPageNum) {
                int pagesToCreate = endGap.getOrder() - beginGap.getOrder() - 1;
                int pagesCreated = 0;
                for (int index = 1; index <= pagesToCreate; index++) {
                    if (1 > endPageNum - index) break;
                    final String newPagePidFromEnd = endPagePrefix + (endPageNum - index);
                    if (!pagesMap.containsKey(newPagePidFromEnd) && !beginPagePrefix.contains(newPagePidFromEnd)) {
                        final GooglePageInfo gapPage = new GooglePageInfo(newPagePidFromEnd, endGap.getOrder() - index);
                        addPage(gapPage);
                        pagesCreated++;
                    } else break;
                }
                if (pagesCreated < pagesToCreate) {
                    pagesToCreate -= pagesCreated;
                    for (int index = 1; index <= pagesToCreate; index++) {
                        final String newPagePidFromBegin = beginPagePrefix + (beginPageNum + index);
                        final GooglePageInfo gapPage = new GooglePageInfo(newPagePidFromBegin, beginGap.getOrder() + index);
                        addPage(gapPage);
                    }
                }
            } else if (1 < beginPageNum && 0 > endPageNum) {
                logger.severe(String.format("Cannot fill gap between pages %s(order=%s) and %s(order=%s)", beginGap.getPid(), beginGap.getOrder(), endGap.getPid(), endGap.getOrder()));
            }
        }

        pagesList.sort(GooglePageInfo::compareTo);
    }

    @Override
    public GooglePageInfo getPageByPid(final String pid) {
        return pagesMap.get(pid);
    }

    @Override
    public GooglePageInfo[] getPages() {
        return pages;
    }

    public void setPages(final GooglePageInfo[] pages) {
        this.pages = pages;
        this.pagesMap = new HashMap<>();

        if (null != pages) for (final GooglePageInfo page : pages)
            pagesMap.put(page.getPid(), page);
    }

    @Override
    public String getMissingPagesList() {
        final Predicate<GooglePageInfo> predicate = pageInfo -> !pageInfo.isFileExists();
        return getListByCondition(predicate);
    }

    private static Pair<GooglePageInfo, GooglePageInfo> createPair(final GooglePageInfo p1, final GooglePageInfo p2) {
        return p1.getOrder() < p2.getOrder() ? new ImmutablePair<>(p1, p2) : new ImmutablePair<>(p2, p1);
    }

    private String getListByCondition(final Predicate<GooglePageInfo> condition) {
        final StringBuilder bList = new StringBuilder();
        final Collection<Pair<GooglePageInfo, GooglePageInfo>> pairs = new ArrayList<>();

        GooglePageInfo blockStart = null, prevPage = null;

        final long filteredCount = pagesList.stream().filter(condition).count();
        final GooglePageInfo lastPage = pagesList.getLast();

        for (final GooglePageInfo currentPage : pagesList) {
            if (condition.test(currentPage)) if (null == blockStart) {
                blockStart = currentPage;
            } else {
            }
            else {
                if (null == blockStart) {
                } else {
                    pairs.add(createPair(blockStart, prevPage));
                    blockStart = null;
                }
            }

            if (currentPage.equals(lastPage) && null != blockStart) pairs.add(createPair(blockStart, currentPage));

            prevPage = currentPage;
        }

        for (final Pair<GooglePageInfo, GooglePageInfo> pair : pairs)
            if (pair.getLeft() == pair.getRight()) bList.append(String.format("%s, ", pair.getLeft().getPid()));
            else bList.append(String.format("%s-%s, ", pair.getLeft().getPid(), pair.getRight().getPid()));

        if (0 < bList.length()) {
            bList.deleteCharAt(bList.length() - 1).deleteCharAt(bList.length() - 1);
            bList.append(String.format(". Total = %d/%d", filteredCount, pagesList.size()));
        }

        return bList.toString();
    }
}
