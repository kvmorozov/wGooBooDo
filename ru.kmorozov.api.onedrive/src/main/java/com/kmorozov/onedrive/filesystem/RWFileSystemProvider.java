package com.kmorozov.onedrive.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;

class RWFileSystemProvider extends ROFileSystemProvider {

    private static void removeRecursive(final Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
                // try to delete the file anyway, even if its attributes
                // could not be read, since delete-only access is
                // theoretically possible
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                if (null == exc) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    // directory iteration failed; propagate exception
                    throw exc;
                }
            }
        });
    }

    @Override
    public void delete(final File file) throws IOException {
        removeRecursive(file.toPath());
    }

    @Override
    public File createFolder(final File file, final String name) throws IOException {
        final File newFolder = new File(file, name);

        if (!newFolder.mkdir()) {
            throw new IOException(String.format("Unable to create local directory '%s' in '%s'", name, file.getName()));
        }

        return newFolder;
    }

    @Override
    public void replaceFile(final File original, final File replacement) throws IOException {
        replaceFileInternal(original, replacement, 0, 10);
    }

    private static void replaceFileInternal(final File original, final File replacement, final int currentTry, final int maxRetries) throws IOException {
        if (currentTry >= maxRetries)
            throw new IOException("Unable to replace local file" + original.getPath());

        if (original.exists() && !original.delete()) {
            try {
                Thread.sleep(500);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            replaceFileInternal(original, replacement, currentTry + 1, maxRetries);
        }

        if (!replacement.renameTo(original)) {
            try {
                Thread.sleep(500);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            replaceFileInternal(original, replacement, currentTry + 1, maxRetries);
        }
    }

    @Override
    public void setAttributes(final File downloadFile, final Date created, final Date lastModified) throws IOException {
        final BasicFileAttributeView attributes = Files.getFileAttributeView(downloadFile.toPath(), BasicFileAttributeView.class);
        final FileTime createdFt = FileTime.fromMillis(created.getTime());
        final FileTime lastModifiedFt = FileTime.fromMillis(lastModified.getTime());
        attributes.setTimes(lastModifiedFt, lastModifiedFt, createdFt);
    }

    @Override
    public boolean verifyCrc(final File file, final long crc) throws IOException {
        return getChecksum(file) == crc;
    }
}
