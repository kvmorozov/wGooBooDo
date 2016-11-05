package ru.kmorozov.gbd.core.logic.connectors;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by km on 17.05.2016.
 */
public interface Response extends Closeable {

    InputStream getContent() throws IOException;

    String getImageFormat();
}
