package ru.kmorozov.gbd.logger.progress

/**
 * Created by km on 06.11.2016.
 */
interface IProgress {

    fun inc(): Int

    fun incrementAndProgress(): Int

    fun finish()

    fun getSubProgress(maxValue: Int): IProgress

    fun resetMaxValue(maxValue: Int)
}
