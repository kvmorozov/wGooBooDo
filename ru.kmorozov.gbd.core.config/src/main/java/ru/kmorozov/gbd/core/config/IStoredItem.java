package ru.kmorozov.gbd.core.config;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public interface IStoredItem {

    OutputStream getOutputStream() throws IOException;

    boolean exists() throws IOException;

    void delete() throws IOException;

    void close() throws IOException;

    void write(byte[] bytes, int read) throws IOException;

    File asFile();
}
