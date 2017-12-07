package com.wouterbreukink.onedrive.client.walker;

import com.wouterbreukink.onedrive.client.OneDriveItem;
import com.wouterbreukink.onedrive.client.OneDriveProvider;

import java.io.IOException;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */
public class OneDriveWalkers {

    private static final int MAX_MAX_DEPTH = 100;
    private static OneDriveIterator<OneDriveItem> itr;

    public static Stream<OneDriveItem> walk(OneDriveProvider api, int maxDepth, Predicate<OneDriveItem> skipCondition) throws IOException {
        itr = new OneDriveIterator(api, api.getRoot(), maxDepth, skipCondition);

        try {
            Stream<OneDriveItem> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(itr, 1), false);
            return stream.onClose(itr::close);
        } catch (RuntimeException | Error ex) {
            itr.close();
            throw ex;
        }
    }

    public static Stream<OneDriveItem> walk(OneDriveProvider api, int maxDepth) throws IOException {
        return walk(api, maxDepth, x -> false);
    }

    public static Stream<OneDriveItem> walk(OneDriveProvider api, Predicate<OneDriveItem> skipCondition) throws IOException {
        return walk(api, MAX_MAX_DEPTH, skipCondition);
    }

    public static void stopAll() {
        itr.close();
    }
}
