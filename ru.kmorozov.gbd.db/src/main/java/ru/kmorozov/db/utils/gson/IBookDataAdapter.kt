package ru.kmorozov.db.utils.gson

import com.google.gson.*
import ru.kmorozov.db.core.logic.model.book.google.GoogleBookData
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData
import ru.kmorozov.db.core.logic.model.book.shpl.ShplBookData

import java.lang.reflect.Type

/**
 * Created by sbt-morozov-kv on 17.11.2016.
 */
class IBookDataAdapter : JsonSerializer<IBookData>, JsonDeserializer<IBookData> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): IBookData {
        val jsonObject = json.asJsonObject

        return context.deserialize(jsonObject, if (null == jsonObject.get("fullview")) ShplBookData::class.java else GoogleBookData::class.java)
    }

    override fun serialize(src: IBookData, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return context.serialize(src)
    }
}
