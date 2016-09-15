package ru.simpleGBD.App.Config;

import ru.simpleGBD.App.GUI.MainBookForm;

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
    private static final String KEY_FILL_GAPS = "fill.gaps";
    private static final String KEY_SECURE_MODE = "secure.mode";

    private static final Preferences preferences = Preferences.userRoot().node(PREFERENCES_NODE);

    private static String getStringProperty(String propKey) {return preferences.get(propKey, null);}
    private static void setStringProperty(String propKey, String value) {preferences.put(propKey, value);}

    private static int getIntProperty(String propKey) {return  preferences.getInt(propKey, 0);}
    private static void setIntProperty(String propKey, int value) {preferences.putInt(propKey, value);}

    private static boolean getBoolProperty(String propKey) {return  preferences.getBoolean(propKey, false);}
    private static void setBoolProperty(String propKey, boolean value) {preferences.putBoolean(propKey, value);}

    public static String getRootDir() {return getStringProperty(KEY_ROOT_DIR);}
    public static void setRootDir(String value) {setStringProperty(KEY_ROOT_DIR, value);}

    public static String getProxyListFile() {return getStringProperty(KEY_PROXY_LIST_FILE);}
    public static void setProxyListFile(String value) {setStringProperty(KEY_PROXY_LIST_FILE, value);}

    public static String getLastBookId() {return getStringProperty(KEY_LAST_BOOK_ID);}
    public static void setLastBookId(String value) {setStringProperty(KEY_LAST_BOOK_ID, value);}

    public static int getResolution() {return getIntProperty(KEY_RESOLUTION);}
    public static void setResolution(int value) {setIntProperty(KEY_RESOLUTION, value);}

    public static boolean getReload() {return getBoolProperty(KEY_RELOAD);}
    public static void setReload(boolean value) {setBoolProperty(KEY_RELOAD, value);}

    public static boolean getFillGaps() {return getBoolProperty(KEY_FILL_GAPS);}
    public static void setFillGaps(boolean value) {setBoolProperty(KEY_FILL_GAPS, value);}

    public static boolean getSecureMode() {return getBoolProperty(KEY_SECURE_MODE);}
    public static void setSecureMode(boolean value) {setBoolProperty(KEY_SECURE_MODE, value);}

    public static boolean isGuiMode() {return MainBookForm.getINSTANCE() != null;}
}
