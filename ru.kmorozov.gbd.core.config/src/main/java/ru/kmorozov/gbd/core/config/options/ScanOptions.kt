package ru.kmorozov.gbd.core.config.options

class ScanOptions(val scanEnabled: Boolean, val tessDataPath: String) {

    companion object {
        val DEFAULT_SCAN_OPTIONS = ScanOptions(false, "")
    }
}