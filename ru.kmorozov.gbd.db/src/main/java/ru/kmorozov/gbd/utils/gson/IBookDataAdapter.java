package ru.kmorozov.gbd.utils.gson;

import com.google.gson.*;
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData;
import ru.kmorozov.gbd.core.logic.model.book.google.GoogleBookData;
import ru.kmorozov.gbd.core.logic.model.book.shpl.ShplBookData;

import java.lang.reflect.Type;

/**
 * Created by sbt-morozov-kv on 17.11.2016.
 */
public class IBookDataAdapter implements JsonSerializer<IBookData>, JsonDeserializer<IBookData> {

    @Override
    public IBookData deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        return context.deserialize(jsonObject, null == jsonObject.get("fullview") ? ShplBookData.class : GoogleBookData.class);
    }

    @Override
    public JsonElement serialize(final IBookData src, final Type typeOfSrc, final JsonSerializationContext context) {
        return context.serialize(src);
    }
}