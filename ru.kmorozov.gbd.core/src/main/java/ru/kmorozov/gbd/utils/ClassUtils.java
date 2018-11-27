package ru.kmorozov.gbd.utils;

/**
 * Created by km on 23.12.2016.
 */
public class ClassUtils {

    public static boolean isClassExists(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return null != clazz;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
