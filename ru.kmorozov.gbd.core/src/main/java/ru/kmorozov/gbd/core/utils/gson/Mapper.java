package ru.kmorozov.gbd.core.utils.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo;

/**
 * Created by km on 21.11.2015.
 */
public class Mapper {

    private static volatile Gson gson;
    private static final Object lockObj = new Object();

    public static Gson getGson() {
        if (gson == null) {
            synchronized (lockObj) {
                if (gson == null) {
                    GsonBuilder builder = new GsonBuilder();
                    builder.registerTypeAdapter(IBookData.class, new IBookDataAdapter());
                    builder.registerTypeAdapter(IPagesInfo.class, new IPagesInfoAdapter());
                    builder.registerTypeAdapter(IPage.class, new IPageAdapter());
                    gson = builder.create();
                }
            }
        }

        return gson;
    }

}
