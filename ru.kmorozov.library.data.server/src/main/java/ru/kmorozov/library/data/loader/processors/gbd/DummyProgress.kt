package ru.kmorozov.library.data.loader.processors.gbd

import ru.kmorozov.gbd.logger.progress.IProgress

class DummyProgress : IProgress {

    override fun inc(): Int {
        return 0
    }

    override fun incrementAndProgress(): Int {
        return 0
    }

    override fun finish() {

    }

    override fun getSubProgress(maxValue: Int): IProgress {
        return DummyProgress()
    }

    override fun resetMaxValue(maxValue: Int) {

    }
}
