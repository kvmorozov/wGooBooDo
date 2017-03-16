package com.wouterbreukink.onedrive.client.walker;

import com.wouterbreukink.onedrive.client.OneDriveItem;
import com.wouterbreukink.onedrive.client.OneDriveProvider;

import java.io.IOException;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */
public class OneDriveWalkers {

    private static final int MAX_MAX_DEPTH = 100;

    public static Stream<OneDriveItem> walk(OneDriveProvider api, int maxDepth) throws IOException {
        OneDriveIterator<OneDriveItem> itr = new OneDriveIterator(api, api.getRoot(), maxDepth);

        try {
            Stream stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(itr, 1), false);
            return (Stream<OneDriveItem>) stream.onClose(itr::close);
        } catch (RuntimeException | Error ex) {
            itr.close();
            throw ex;
        }
    }

    public static Stream<OneDriveItem> walk(OneDriveProvider api) throws IOException {
        return walk(api, MAX_MAX_DEPTH);
    }
}
