package ru.kmorozov.gbd.desktop.output.progress

import ru.kmorozov.gbd.core.config.SystemConfigs
import ru.kmorozov.gbd.logger.progress.IProgress
import ru.kmorozov.gbd.desktop.gui.MainBookForm

import javax.swing.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by km on 22.12.2015.
 */
class ProcessStatus : IProgress {

    private val value = AtomicInteger(0)
    var progressBar: JProgressBar? = null
        private set
    private var maxValue: Int = 0

    constructor(maxValue: Int) {
        this.maxValue = maxValue

        start()
    }

    constructor() {

    }

    override fun resetMaxValue(maxValue: Int) {
        this.maxValue = maxValue

        start()
    }

    override fun inc(): Int {
        return value.incrementAndGet()
    }

    fun get(): Int {
        return value.get()
    }

    override fun incrementAndProgress(): Int {
        return if (0 == maxValue) 0 else Math.round(Math.min(inc() * 100 / maxValue, 100).toFloat())
    }

    private fun start() {
        if (SystemConfigs.isConsoleMode) return

        progressBar = JProgressBar()
        progressBar!!.minimum = 0
        progressBar!!.minimum = maxValue
        progressBar!!.isIndeterminate = false

        SwingUtilities.invokeLater {
            MainBookForm.instance!!.progressPanel!!.add(progressBar)
            SwingUtilities.updateComponentTreeUI(MainBookForm.instance!!.progressPanel!!)
        }
    }

    override fun finish() {
        if (SystemConfigs.isConsoleMode) return

        SwingUtilities.invokeLater {
            MainBookForm.instance!!.progressPanel!!.remove(progressBar!!)
            SwingUtilities.updateComponentTreeUI(MainBookForm.instance!!.progressPanel!!)
        }
    }

    override fun getSubProgress(maxValue: Int): IProgress {
        return ProcessStatus(maxValue)
    }
}
