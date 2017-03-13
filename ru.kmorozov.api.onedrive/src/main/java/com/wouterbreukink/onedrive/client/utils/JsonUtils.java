package com.wouterbreukink.onedrive.client.utils;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

/**
 * Created by sbt-morozov-kv on 13.03.2017.
 */
public class JsonUtils {

    public static final JsonFactory JSON_FACTORY = new GsonFactory();
}
