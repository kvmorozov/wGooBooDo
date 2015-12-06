package ru.simpleGBD.App.Logic.Runtime;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

/**
 * Created by km on 05.12.2015.
 */
public class AbstractHttpProcessor {

    private static int MAX_RETRY_COUNT = 2;
    private static int SLEEP_TIME = 100;

    protected HttpResponse getResponse(HttpClient instance, HttpGet request) {
        try {
            return instance.execute(request);
        } catch (Exception e) {
            for (int i = 1; i <= MAX_RETRY_COUNT; i++) {
                try {
                    return instance.execute(request);
                } catch (Exception ex) {
                    try {
                        Thread.sleep(SLEEP_TIME * i);
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }

        return null;
    }
}
