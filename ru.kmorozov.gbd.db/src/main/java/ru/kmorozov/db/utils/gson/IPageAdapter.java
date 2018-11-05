package ru.kmorozov.db.utils.gson;

import com.google.gson.*;
import ru.kmorozov.db.core.logic.model.book.google.GooglePageInfo;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.db.core.logic.model.book.shpl.ShplPage;

import java.lang.reflect.Type;

/**
 * Created by sbt-morozov-kv on 17.11.2016.
 */
public class IPageAdapter implements JsonSerializer<IPage>, JsonDeserializer<IPage> {

    @Override
    public IPage deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        return context.deserialize(jsonObject, null == jsonObject.get("pid") ? ShplPage.class : GooglePageInfo.class);
    }

    @Override
    public JsonElement serialize(final IPage src, final Type typeOfSrc, final JsonSerializationContext context) {
        return context.serialize(src);
    }
}
