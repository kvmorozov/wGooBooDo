package ru.kmorozov.gbd.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo;
import ru.kmorozov.gbd.utils.gson.IBookDataAdapter;
import ru.kmorozov.gbd.utils.gson.IPageAdapter;
import ru.kmorozov.gbd.utils.gson.IPagesInfoAdapter;

/**
 * Created by km on 21.11.2015.
 */
public class Mapper {

    private static final Object lockObj = new Object();
    private static volatile Gson gson;

    public static Gson getGson() {
        if (null == gson) {
            synchronized (lockObj) {
                if (null == gson) {
                    final GsonBuilder builder = new GsonBuilder();
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
