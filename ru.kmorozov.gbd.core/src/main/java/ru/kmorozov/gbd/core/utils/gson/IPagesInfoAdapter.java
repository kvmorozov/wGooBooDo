package ru.kmorozov.gbd.core.utils.gson;

import com.google.gson.*;
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo;
import ru.kmorozov.gbd.core.logic.model.book.google.GooglePagesInfo;
import ru.kmorozov.gbd.core.logic.model.book.shpl.ShplPagesInfo;

import java.lang.reflect.Type;

/**
 * Created by sbt-morozov-kv on 17.11.2016.
 */
public class IPagesInfoAdapter implements JsonSerializer<IPagesInfo>, JsonDeserializer<IPagesInfo> {

    @Override
    public IPagesInfo deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        return context.deserialize(jsonObject, null == jsonObject.get("prefix") ? ShplPagesInfo.class : GooglePagesInfo.class);
    }

    @Override
    public JsonElement serialize(final IPagesInfo src, final Type typeOfSrc, final JsonSerializationContext context) {
        return context.serialize(src);
    }
}
