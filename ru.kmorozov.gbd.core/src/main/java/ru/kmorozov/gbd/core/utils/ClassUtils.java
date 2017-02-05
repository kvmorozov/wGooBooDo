package ru.kmorozov.gbd.core.utils;

/**
 * Created by km on 23.12.2016.
 */
public class ClassUtils {

    public static boolean isClassExists(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return clazz != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
