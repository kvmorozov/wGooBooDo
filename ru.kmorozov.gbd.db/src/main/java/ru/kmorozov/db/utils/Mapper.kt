package ru.kmorozov.db.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import ru.kmorozov.db.utils.gson.IBookDataAdapter
import ru.kmorozov.db.utils.gson.IPageAdapter
import ru.kmorozov.db.utils.gson.IPagesInfoAdapter
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData
import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo

import java.lang.reflect.Type

/**
 * Created by km on 21.11.2015.
 */
object Mapper {

    var mapType = object : TypeToken<Map<String, String>>() {

    }.type

    private val lockObj = Any()
    @Volatile
    private var gson: Gson? = null

    fun getGson(): Gson? {
        if (null == gson) {
            synchronized(lockObj) {
                if (null == gson) {
                    val builder = GsonBuilder()
                    builder.registerTypeAdapter(IBookData::class.java, IBookDataAdapter())
                    builder.registerTypeAdapter(IPagesInfo::class.java, IPagesInfoAdapter())
                    builder.registerTypeAdapter(IPage::class.java, IPageAdapter())
                    gson = builder.create()
                }
            }
        }

        return gson
    }

}
