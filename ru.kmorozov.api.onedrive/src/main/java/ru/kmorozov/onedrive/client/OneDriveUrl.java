package ru.kmorozov.onedrive.client;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;
import ru.kmorozov.onedrive.client.resources.Drive;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class OneDriveUrl extends GenericUrl {

    private static final String rootUrl = "https://graph.microsoft.com/v1.0";

    @Key("$skiptoken")
    private String token;

    public OneDriveUrl(String encodedUrl) {
        super(encodedUrl);
    }

    public static OneDriveUrl defaultDrive() {
        return new OneDriveUrl(OneDriveUrl.rootUrl + "/drive");
    }

    public static OneDriveUrl driveRoot() {
        return new OneDriveUrl(OneDriveUrl.rootUrl + "/drive/root");
    }

    public static OneDriveUrl children(String id) {
        return new OneDriveUrl(OneDriveUrl.rootUrl + "/drive/items/" + id + "/children");
    }

    public static OneDriveUrl putContent(String id, String name) {
        return new OneDriveUrl(OneDriveUrl.rootUrl + "/drive/items/" + id + ":/" + OneDriveUrl.encode(name) + ":/content");
    }

    public static OneDriveUrl postMultiPart(String id) {
        return new OneDriveUrl(OneDriveUrl.rootUrl + "/drive/items/" + id + "/children");
    }

    public static OneDriveUrl createUploadSession(String id, String name) {
        return new OneDriveUrl(OneDriveUrl.rootUrl + "/drive/items/" + id + ":/" + OneDriveUrl.encode(name) + ":/upload.createSession");
    }

    public static OneDriveUrl getPath(String path) {
        return new OneDriveUrl(OneDriveUrl.rootUrl + "/drive/root:/" + OneDriveUrl.encode(path).replace("%5C", "/"));
    }

    public static GenericUrl item(String id) {
        return new OneDriveUrl(OneDriveUrl.rootUrl + "/drive/items/" + id);
    }

    public static GenericUrl content(String id) {
        return new OneDriveUrl(OneDriveUrl.rootUrl + "/drive/items/" + id + "/content");
    }

    public static OneDriveUrl search(Drive drive, String query) {
        return new OneDriveUrl(OneDriveUrl.rootUrl + "/drives/" + drive.getId() + "/root/search(q='{" + query + "}')");
    }

    private static String encode(String url) {
        return URLEncoder.encode(url, StandardCharsets.UTF_8);
    }

    public void setToken(String token) {
        this.token = token;
    }
}

