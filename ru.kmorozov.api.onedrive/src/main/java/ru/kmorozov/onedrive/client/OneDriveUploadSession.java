package ru.kmorozov.onedrive.client;

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
    private OneDriveUploadSession.Range[] ranges;
    private long totalUploaded;
    private long lastUploaded;
    private OneDriveItem item;

    public OneDriveUploadSession(OneDriveItem parent, File file, String uploadUrl, String[] ranges) throws IOException {
        this.parent = parent;
        this.file = file;
        this.uploadUrl = uploadUrl;
        raf = new RandomAccessFile(file, "r");
        this.setRanges(ranges);
    }

    public void setRanges(String[] stringRanges) {

        ranges = new OneDriveUploadSession.Range[stringRanges.length];
        for (int i = 0; i < stringRanges.length; i++) {
            long start = Long.parseLong(stringRanges[i].substring(0, stringRanges[i].indexOf('-')));

            String s = stringRanges[i].substring(stringRanges[i].indexOf('-') + 1);

            long end = 0L;
            if (!s.isEmpty()) {
                end = Long.parseLong(s);
            }

            this.ranges[i] = new OneDriveUploadSession.Range(start, end);
        }

        if (0 < this.ranges.length) {
            this.lastUploaded = this.ranges[0].start - this.totalUploaded;
            this.totalUploaded = this.ranges[0].start;
        }
    }

    public byte[] getChunk() throws IOException {

        byte[] bytes = new byte[OneDriveUploadSession.CHUNK_SIZE];

        this.raf.seek(this.totalUploaded);
        int read = this.raf.read(bytes);

        if (OneDriveUploadSession.CHUNK_SIZE > read) {
            bytes = Arrays.copyOf(bytes, read);
        }

        return bytes;
    }

    public long getTotalUploaded() {
        return this.totalUploaded;
    }

    public long getLastUploaded() {
        return this.lastUploaded;
    }

    public OneDriveItem getParent() {
        return this.parent;
    }

    public String getUploadUrl() {
        return this.uploadUrl;
    }

    public File getFile() {
        return this.file;
    }

    public boolean isComplete() {
        return null != this.item;
    }

    public void setComplete(OneDriveItem item) {
        this.item = item;
        this.lastUploaded = this.file.length() - this.totalUploaded;
        this.totalUploaded = this.file.length();
    }

    public OneDriveItem getItem() {
        return this.item;
    }

    private static final class Range {
        public long start;
        public long end;

        private Range(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }
}
