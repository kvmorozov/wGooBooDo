package ru.kmorozov.gbd.core.config;

import java.util.prefs.Preferences;

/**
 * Created by km on 06.12.2015.
 */
public class SystemConfigs {

    private static final String PREFERENCES_NODE = "ru/simpleGBD/wGooBooDo";
    private static final String KEY_ROOT_DIR = "out.root.dir";
    private static final String KEY_PROXY_LIST_FILE = "proxy.list.file";
    private static final String KEY_LAST_BOOK_ID = "last.book.id";
    private static final String KEY_RESOLUTION = "resolution";
    private static final String KEY_RELOAD = "reload";
    private static final String KEY_SECURE_MODE = "secure.mode";
    private static final String KEY_PDF_MODE = "pdf.mode";

    private static final Preferences preferences = Preferences.userRoot().node(PREFERENCES_NODE);

    private static String getStringProperty(final String propKey) {
        return preferences.get(propKey, null);
    }

    private static void setStringProperty(final String propKey, final String value) {
        preferences.put(propKey, value);
    }

    private static int getIntProperty(final String propKey) {
        return preferences.getInt(propKey, 0);
    }

    private static void setIntProperty(final String propKey, final int value) {
        preferences.putInt(propKey, value);
    }

    private static boolean getBoolProperty(final String propKey) {
        return preferences.getBoolean(propKey, false);
    }

    private static void setBoolProperty(final String propKey, final boolean value) {
        preferences.putBoolean(propKey, value);
    }

    public static String getRootDir() {
        return getStringProperty(KEY_ROOT_DIR);
    }

    public static void setRootDir(final String value) {
        setStringProperty(KEY_ROOT_DIR, value);
    }

    public static String getProxyListFile() {
        return getStringProperty(KEY_PROXY_LIST_FILE);
    }

    public static void setProxyListFile(final String value) {
        setStringProperty(KEY_PROXY_LIST_FILE, value);
    }

    public static String getLastBookId() {
        return getStringProperty(KEY_LAST_BOOK_ID);
    }

    public static void setLastBookId(final String value) {
        setStringProperty(KEY_LAST_BOOK_ID, value);
    }

    public static int getResolution() {
        return getIntProperty(KEY_RESOLUTION);
    }

    public static void setResolution(final int value) {
        setIntProperty(KEY_RESOLUTION, value);
    }

    public static boolean getReload() {
        return getBoolProperty(KEY_RELOAD);
    }

    public static void setReload(final boolean value) {
        setBoolProperty(KEY_RELOAD, value);
    }

    public static boolean getSecureMode() {
        return getBoolProperty(KEY_SECURE_MODE);
    }

    public static void setSecureMode(final boolean value) {
        setBoolProperty(KEY_SECURE_MODE, value);
    }

    public static boolean isConsoleMode() {
        return true;
    }

    public static String getPdfMode() {
        return getStringProperty(KEY_PDF_MODE);
    }

    public static void setPdfMode(final String value) {
        setStringProperty(KEY_PDF_MODE, value);
    }
}
