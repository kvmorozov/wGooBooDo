package ru.kmorozov.gbd.core.config.options

enum class PdfOptions private constructor(private val pdfMode: String) {
    CREATE("PDF_CREATE"),
    SKIP("PDF_SKIP");


    companion object {
        val DEFAULT_MODE = CREATE

        fun getOption(pdfMode: String): PdfOptions {
            for (option in PdfOptions.values())
                if (option.pdfMode.equals(pdfMode, ignoreCase = true))
                    return option

            return DEFAULT_MODE
        }
    }
}
