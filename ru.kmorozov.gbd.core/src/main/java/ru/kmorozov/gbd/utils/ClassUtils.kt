package ru.kmorozov.gbd.utils

/**
 * Created by km on 23.12.2016.
 */
object ClassUtils {

    fun isClassExists(className: String): Boolean {
        try {
            val clazz = Class.forName(className)
            return null != clazz
        } catch (e: ClassNotFoundException) {
            return false
        }

    }
}
