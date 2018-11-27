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

    private static final Preferences preferences = Preferences.userRoot().node(SystemConfigs.PREFERENCES_NODE);

    private static String getStringProperty(String propKey) {
        return SystemConfigs.preferences.get(propKey, null);
    }

    private static void setStringProperty(String propKey, String value) {
        SystemConfigs.preferences.put(propKey, value);
    }

    private static int getIntProperty(String propKey) {
        return SystemConfigs.preferences.getInt(propKey, 0);
    }

    private static void setIntProperty(String propKey, int value) {
        SystemConfigs.preferences.putInt(propKey, value);
    }

    private static boolean getBoolProperty(String propKey) {
        return SystemConfigs.preferences.getBoolean(propKey, false);
    }

    private static void setBoolProperty(String propKey, boolean value) {
        SystemConfigs.preferences.putBoolean(propKey, value);
    }

    public static String getRootDir() {
        return SystemConfigs.getStringProperty(SystemConfigs.KEY_ROOT_DIR);
    }

    public static void setRootDir(String value) {
        SystemConfigs.setStringProperty(SystemConfigs.KEY_ROOT_DIR, value);
    }

    public static String getProxyListFile() {
        return SystemConfigs.getStringProperty(SystemConfigs.KEY_PROXY_LIST_FILE);
    }

    public static void setProxyListFile(String value) {
        SystemConfigs.setStringProperty(SystemConfigs.KEY_PROXY_LIST_FILE, value);
    }

    public static String getLastBookId() {
        return SystemConfigs.getStringProperty(SystemConfigs.KEY_LAST_BOOK_ID);
    }

    public static void setLastBookId(String value) {
        SystemConfigs.setStringProperty(SystemConfigs.KEY_LAST_BOOK_ID, value);
    }

    public static int getResolution() {
        return SystemConfigs.getIntProperty(SystemConfigs.KEY_RESOLUTION);
    }

    public static void setResolution(int value) {
        SystemConfigs.setIntProperty(SystemConfigs.KEY_RESOLUTION, value);
    }

    public static boolean getReload() {
        return SystemConfigs.getBoolProperty(SystemConfigs.KEY_RELOAD);
    }

    public static void setReload(boolean value) {
        SystemConfigs.setBoolProperty(SystemConfigs.KEY_RELOAD, value);
    }

    public static boolean getSecureMode() {
        return SystemConfigs.getBoolProperty(SystemConfigs.KEY_SECURE_MODE);
    }

    public static void setSecureMode(boolean value) {
        SystemConfigs.setBoolProperty(SystemConfigs.KEY_SECURE_MODE, value);
    }

    public static boolean isConsoleMode() {
        return true;
    }

    public static String getPdfMode() {
        return SystemConfigs.getStringProperty(SystemConfigs.KEY_PDF_MODE);
    }

    public static void setPdfMode(String value) {
        SystemConfigs.setStringProperty(SystemConfigs.KEY_PDF_MODE, value);
    }
}
