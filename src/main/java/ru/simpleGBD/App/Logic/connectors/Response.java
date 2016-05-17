package ru.simpleGBD.App.Logic.connectors;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by km on 17.05.2016.
 */
public interface Response {

    InputStream getContent() throws IOException;

    String getImageFormat();
}
