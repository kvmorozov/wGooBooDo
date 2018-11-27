package ru.kmorozov.db.utils.gson;

import com.google.gson.*;
import ru.kmorozov.db.core.logic.model.book.google.GooglePagesInfo;
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo;
import ru.kmorozov.db.core.logic.model.book.shpl.ShplPagesInfo;

import java.lang.reflect.Type;

/**
 * Created by sbt-morozov-kv on 17.11.2016.
 */
public class IPagesInfoAdapter implements JsonSerializer<IPagesInfo>, JsonDeserializer<IPagesInfo> {

    @Override
    public IPagesInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        return context.deserialize(jsonObject, null == jsonObject.get("prefix") ? ShplPagesInfo.class : GooglePagesInfo.class);
    }

    @Override
    public JsonElement serialize(IPagesInfo src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src);
    }
}
