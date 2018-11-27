package ru.kmorozov.onedrive.filesystem;

import ru.kmorozov.onedrive.CommandLineOpts;

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

    public void delete(File file) throws IOException {
        // Do nothing
    }

    public File createFolder(File file, String name) throws IOException {

        return new File(file, name) {
            @Override
            public boolean isDirectory() {
                return true;
            }
        };
    }

    public File createFile(File file, String name) {
        return new File(file, name);
    }

    public void replaceFile(File original, File replacement) throws IOException {
        // Do nothing
    }

    public void setAttributes(File downloadFile, Date created, Date lastModified) throws IOException {
        // Do nothing
    }

    public boolean verifyCrc(File file, long crc) throws IOException {
        return true;
    }

    public FileSystemProvider.FileMatch verifyMatch(File file, long crc, long fileSize, Date created, Date lastModified) throws IOException {

        // Round to nearest second
        created = new Date((created.getTime() / 1000L) * 1000L);
        lastModified = new Date((lastModified.getTime() / 1000L) * 1000L);

        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);

        // Timestamp rounded to the nearest second
        Date localCreatedDate = new Date(attr.creationTime().to(TimeUnit.SECONDS) * 1000L);
        Date localModifiedDate = new Date(attr.lastModifiedTime().to(TimeUnit.SECONDS) * 1000L);

        boolean sizeMatches = fileSize == file.length();
        boolean createdMatches = created.equals(localCreatedDate);
        boolean modifiedMatches = lastModified.equals(localModifiedDate);

        if (!CommandLineOpts.getCommandLineOpts().useHash() && sizeMatches && createdMatches && modifiedMatches) {
            // Close enough!
            return FileSystemProvider.FileMatch.YES;
        }

        long localCrc = this.getChecksum(file);
        boolean crcMatches = crc == localCrc;

        // If the crc matches but the timestamps do not we won't upload the content again
        if (crcMatches && !(modifiedMatches && createdMatches)) {
            return FileSystemProvider.FileMatch.CRC;
        }
        else if (crcMatches) {
            return FileSystemProvider.FileMatch.YES;
        }
        else {
            return FileSystemProvider.FileMatch.NO;
        }
    }

    public FileSystemProvider.FileMatch verifyMatch(File file, Date created, Date lastModified) throws IOException {

        // Round to nearest second
        created = new Date((created.getTime() / 1000L) * 1000L);
        lastModified = new Date((lastModified.getTime() / 1000L) * 1000L);

        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);

        // Timestamp rounded to the nearest second
        Date localCreatedDate = new Date(attr.creationTime().to(TimeUnit.SECONDS) * 1000L);
        Date localModifiedDate = new Date(attr.lastModifiedTime().to(TimeUnit.SECONDS) * 1000L);

        boolean createdMatches = created.equals(localCreatedDate);
        boolean modifiedMatches = lastModified.equals(localModifiedDate);

        if (createdMatches && modifiedMatches) {
            return FileSystemProvider.FileMatch.YES;
        }
        else {
            return FileSystemProvider.FileMatch.NO;
        }
    }

    public long getChecksum(File file) throws IOException {
        // Compute CRC32 checksum
        try (final CheckedInputStream cis = new CheckedInputStream(new FileInputStream(file), new CRC32())) {
            byte[] buf = new byte[1024];

            while (0 <= cis.read(buf)) {
            }

            return cis.getChecksum().getValue();
        }
    }
}
