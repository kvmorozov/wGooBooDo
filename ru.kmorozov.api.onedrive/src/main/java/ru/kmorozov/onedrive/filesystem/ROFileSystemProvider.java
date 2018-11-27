package ru.kmorozov.onedrive.filesystem;

import ru.kmorozov.onedrive.CommandLineOpts;
import ru.kmorozov.onedrive.filesystem.FileSystemProvider.FileMatch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

class ROFileSystemProvider implements FileSystemProvider {

    public void delete(final File file) throws IOException {
        // Do nothing
    }

    public File createFolder(final File file, final String name) throws IOException {

        return new File(file, name) {
            @Override
            public boolean isDirectory() {
                return true;
            }
        };
    }

    public File createFile(final File file, final String name) {
        return new File(file, name);
    }

    public void replaceFile(final File original, final File replacement) throws IOException {
        // Do nothing
    }

    public void setAttributes(final File downloadFile, final Date created, final Date lastModified) throws IOException {
        // Do nothing
    }

    public boolean verifyCrc(final File file, final long crc) throws IOException {
        return true;
    }

    public FileMatch verifyMatch(final File file, final long crc, final long fileSize, Date created, Date lastModified) throws IOException {

        // Round to nearest second
        created = new Date((created.getTime() / 1000L) * 1000L);
        lastModified = new Date((lastModified.getTime() / 1000L) * 1000L);

        final BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);

        // Timestamp rounded to the nearest second
        final Date localCreatedDate = new Date(attr.creationTime().to(TimeUnit.SECONDS) * 1000L);
        final Date localModifiedDate = new Date(attr.lastModifiedTime().to(TimeUnit.SECONDS) * 1000L);

        final boolean sizeMatches = fileSize == file.length();
        final boolean createdMatches = created.equals(localCreatedDate);
        final boolean modifiedMatches = lastModified.equals(localModifiedDate);

        if (!CommandLineOpts.getCommandLineOpts().useHash() && sizeMatches && createdMatches && modifiedMatches) {
            // Close enough!
            return FileMatch.YES;
        }

        final long localCrc = getChecksum(file);
        final boolean crcMatches = crc == localCrc;

        // If the crc matches but the timestamps do not we won't upload the content again
        if (crcMatches && !(modifiedMatches && createdMatches)) {
            return FileMatch.CRC;
        }
        else if (crcMatches) {
            return FileMatch.YES;
        }
        else {
            return FileMatch.NO;
        }
    }

    public FileMatch verifyMatch(final File file, Date created, Date lastModified) throws IOException {

        // Round to nearest second
        created = new Date((created.getTime() / 1000L) * 1000L);
        lastModified = new Date((lastModified.getTime() / 1000L) * 1000L);

        final BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);

        // Timestamp rounded to the nearest second
        final Date localCreatedDate = new Date(attr.creationTime().to(TimeUnit.SECONDS) * 1000L);
        final Date localModifiedDate = new Date(attr.lastModifiedTime().to(TimeUnit.SECONDS) * 1000L);

        final boolean createdMatches = created.equals(localCreatedDate);
        final boolean modifiedMatches = lastModified.equals(localModifiedDate);

        if (createdMatches && modifiedMatches) {
            return FileMatch.YES;
        }
        else {
            return FileMatch.NO;
        }
    }

    public long getChecksum(final File file) throws IOException {
        // Compute CRC32 checksum
        try (CheckedInputStream cis = new CheckedInputStream(new FileInputStream(file), new CRC32())) {
            final byte[] buf = new byte[1024];

            while (0 <= cis.read(buf)) {
            }

            return cis.getChecksum().getValue();
        }
    }
}
