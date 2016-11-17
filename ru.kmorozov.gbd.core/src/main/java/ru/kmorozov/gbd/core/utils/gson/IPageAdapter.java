package ru.kmorozov.gbd.core.utils.gson;

import com.google.gson.*;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.gbd.core.logic.model.book.google.GogglePageInfo;
import ru.kmorozov.gbd.core.logic.model.book.shpl.ShplPage;

import java.lang.reflect.Type;

/**
 * Created by sbt-morozov-kv on 17.11.2016.
 */
public class IPageAdapter implements JsonSerializer<IPage> ,JsonDeserializer<IPage> {

    @Override
    public IPage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        return context.deserialize(jsonObject, jsonObject.get("pid") == null ? ShplPage.class : GogglePageInfo.class);
    }

    @Override
    public JsonElement serialize(IPage src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src);
    }
}
