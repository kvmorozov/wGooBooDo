package ru.kmorozov.db.utils.gson

import com.google.gson.*
import ru.kmorozov.db.core.logic.model.book.google.GooglePageInfo
import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import ru.kmorozov.db.core.logic.model.book.shpl.ShplPage

import java.lang.reflect.Type

/**
 * Created by sbt-morozov-kv on 17.11.2016.
 */
class IPageAdapter : JsonSerializer<IPage>, JsonDeserializer<IPage> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): IPage {
        val jsonObject = json.asJsonObject

        return context.deserialize(jsonObject, if (null == jsonObject.get("pid")) ShplPage::class.java else GooglePageInfo::class.java)
    }

    override fun serialize(src: IPage, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return context.serialize(src)
    }
}
