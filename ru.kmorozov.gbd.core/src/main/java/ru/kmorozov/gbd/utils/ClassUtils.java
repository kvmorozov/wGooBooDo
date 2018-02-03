package ru.kmorozov.gbd.utils;

/**
 * Created by km on 23.12.2016.
 */
public class ClassUtils {

    public static boolean isClassExists(final String className) {
        try {
            final Class<?> clazz = Class.forName(className);
            return null != clazz;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }
}
