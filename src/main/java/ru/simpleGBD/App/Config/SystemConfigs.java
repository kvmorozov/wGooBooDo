package ru.simpleGBD.App.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Created by km on 06.12.2015.
 */
public class SystemConfigs {

    private static final String PREFERENCES_NODE = "ru/simpleGBD/wGooBooDo";
    private static final String KEY_ROOT_DIR = "out.root.dir";
    private static final String KEY_PROXY_LIST_FILE = "proxy.list.file";
    private static final String KEY_LAST_BOOK_ID = "last.book.id";

    private static Preferences preferences = Preferences.userRoot().node(PREFERENCES_NODE);

    private static String getStringProperty(String propKey) {return preferences.get(propKey, null);}
    private static void setStringProperty(String propKey, String value) {preferences.put(propKey, value);}

    private static int getIntProperty(String propKey) {return  preferences.getInt(propKey, 0);}
    private static void setIntProperty(String propKey, int value) {preferences.putInt(propKey, value);}

    public static String getRootDir() {return getStringProperty(KEY_ROOT_DIR);}
    public static void setRootDir(String value) {setStringProperty(KEY_ROOT_DIR, value);}

    public static String getProxyListFile() {return getStringProperty(KEY_PROXY_LIST_FILE);}
    public static void setProxyListFile(String value) {setStringProperty(KEY_PROXY_LIST_FILE, value);}

    public static String getLastBookId() {return getStringProperty(KEY_LAST_BOOK_ID);}
    public static void setLastBookId(String value) {setStringProperty(KEY_LAST_BOOK_ID, value);}
}
