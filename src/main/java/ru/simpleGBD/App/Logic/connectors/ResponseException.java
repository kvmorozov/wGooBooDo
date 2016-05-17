package ru.simpleGBD.App.Logic.connectors;

import java.io.IOException;

/**
 * Created by km on 17.05.2016.
 */
public abstract class ResponseException extends IOException {

    public abstract int getStatusCode();
}
