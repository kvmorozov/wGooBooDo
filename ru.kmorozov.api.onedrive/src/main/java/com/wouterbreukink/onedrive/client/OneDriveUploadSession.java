package com.wouterbreukink.onedrive.client;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class OneDriveUploadSession {

    // Upload in chunks of 5MB as per MS recommendation
    private static final int CHUNK_SIZE = 5 * 1024 * 1024;
    private final File file;
    private final String uploadUrl;
    private final RandomAccessFile raf;
    private final OneDriveItem parent;
    private Range[] ranges;
    private long totalUploaded;
    private long lastUploaded;
    private OneDriveItem item;

    public OneDriveUploadSession(final OneDriveItem parent, final File file, final String uploadUrl, final String[] ranges) throws IOException {
        this.parent = parent;
        this.file = file;
        this.uploadUrl = uploadUrl;
        this.raf = new RandomAccessFile(file, "r");
        setRanges(ranges);
    }

    public void setRanges(final String[] stringRanges) {

        this.ranges = new Range[stringRanges.length];
        for (int i = 0; i < stringRanges.length; i++) {
            final long start = Long.parseLong(stringRanges[i].substring(0, stringRanges[i].indexOf('-')));

            final String s = stringRanges[i].substring(stringRanges[i].indexOf('-') + 1);

            long end = 0;
            if (!s.isEmpty()) {
                end = Long.parseLong(s);
            }

            ranges[i] = new Range(start, end);
        }

        if (0 < ranges.length) {
            lastUploaded = ranges[0].start - totalUploaded;
            totalUploaded = ranges[0].start;
        }
    }

    public byte[] getChunk() throws IOException {

        byte[] bytes = new byte[CHUNK_SIZE];

        raf.seek(totalUploaded);
        final int read = raf.read(bytes);

        if (CHUNK_SIZE > read) {
            bytes = Arrays.copyOf(bytes, read);
        }

        return bytes;
    }

    public long getTotalUploaded() {
        return totalUploaded;
    }

    public long getLastUploaded() {
        return lastUploaded;
    }

    public OneDriveItem getParent() {
        return parent;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public File getFile() {
        return file;
    }

    public boolean isComplete() {
        return null != item;
    }

    public void setComplete(final OneDriveItem item) {
        this.item = item;
        lastUploaded = file.length() - totalUploaded;
        totalUploaded = file.length();
    }

    public OneDriveItem getItem() {
        return item;
    }

    private static final class Range {
        public long start;
        public long end;

        private Range(final long start, final long end) {
            this.start = start;
            this.end = end;
        }
    }
}
