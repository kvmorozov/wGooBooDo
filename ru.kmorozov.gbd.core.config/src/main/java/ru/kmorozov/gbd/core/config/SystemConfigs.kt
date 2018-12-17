package ru.kmorozov.gbd.core.config

import java.util.prefs.Preferences

/**
 * Created by km on 06.12.2015.
 */
object SystemConfigs {

    private const val PREFERENCES_NODE = "ru/simpleGBD/wGooBooDo"
    private const val KEY_ROOT_DIR = "out.root.dir"
    private const val KEY_PROXY_LIST_FILE = "proxy.list.file"
    private const val KEY_LAST_BOOK_ID = "last.book.id"
    private const val KEY_RESOLUTION = "resolution"
    private const val KEY_RELOAD = "reload"
    private const val KEY_SECURE_MODE = "secure.mode"
    private const val KEY_PDF_MODE = "pdf.mode"

    private val preferences = Preferences.userRoot().node(PREFERENCES_NODE)

    var rootDir: String
        get() = getStringProperty(KEY_ROOT_DIR)
        set(value) = setStringProperty(KEY_ROOT_DIR, value)

    var proxyListFile: String
        get() = getStringProperty(KEY_PROXY_LIST_FILE)
        set(value) = setStringProperty(KEY_PROXY_LIST_FILE, value)

    var lastBookId: String
        get() = getStringProperty(KEY_LAST_BOOK_ID)
        set(value) = setStringProperty(KEY_LAST_BOOK_ID, value)

    var resolution: Int
        get() = getIntProperty(KEY_RESOLUTION)
        set(value) = setIntProperty(KEY_RESOLUTION, value)

    var reload: Boolean
        get() = getBoolProperty(KEY_RELOAD)
        set(value) = setBoolProperty(KEY_RELOAD, value)

    var secureMode: Boolean
        get() = getBoolProperty(KEY_SECURE_MODE)
        set(value) = setBoolProperty(KEY_SECURE_MODE, value)

    val isConsoleMode: Boolean
        get() = true

    var pdfMode: String
        get() = getStringProperty(KEY_PDF_MODE)
        set(value) = setStringProperty(KEY_PDF_MODE, value)

    private fun getStringProperty(propKey: String): String {
        return preferences.get(propKey, null)
    }

    private fun setStringProperty(propKey: String, value: String) {
        preferences.put(propKey, value)
    }

    private fun getIntProperty(propKey: String): Int {
        return preferences.getInt(propKey, 0)
    }

    private fun setIntProperty(propKey: String, value: Int) {
        preferences.putInt(propKey, value)
    }

    private fun getBoolProperty(propKey: String): Boolean {
        return preferences.getBoolean(propKey, false)
    }

    private fun setBoolProperty(propKey: String, value: Boolean) {
        preferences.putBoolean(propKey, value)
    }
}
