package ru.kmorozov.gbd.core.loader;

import ru.kmorozov.gbd.core.config.IStoredItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

public class RawFileItem implements IStoredItem {

    protected final File outputFile;
    private OutputStream outputStream;

    public RawFileItem(File file) {
        this.outputFile = file;
    }

    public RawFileItem(Path path) {
        this(path.toFile());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return outputStream = outputStream == null ? new FileOutputStream(outputFile) : outputStream;
    }

    @Override
    public boolean exists() throws IOException {
        return outputFile.exists();
    }

    @Override
    public void delete() throws IOException {
        outputFile.delete();
    }

    @Override
    public void close() throws IOException {
        if (outputStream != null)
            outputStream.close();
    }

    @Override
    public void write(byte[] bytes, int read) throws IOException {
        getOutputStream().write(bytes, 0, read);
    }

    @Override
    public File asFile() {
        return outputFile;
    }
}
