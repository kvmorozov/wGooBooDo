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

/**
 * Created by km on 21.11.2015.
 */
object Mapper {

    var mapType = object : TypeToken<Map<String, String>>() {

    }.type!!

    val gson: Gson
        get() = GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(IBookData::class.java, IBookDataAdapter())
            .registerTypeAdapter(IPagesInfo::class.java, IPagesInfoAdapter())
            .registerTypeAdapter(IPage::class.java, IPageAdapter())
            .create()
}
