package ru.kmorozov.gbd.core.config.options

enum class PdfOptions(private val pdfMode: String) {
    CREATE("PDF_CREATE"),
    CREATE_ONLY("PDF_CREATE_ONLY"),
    SKIP("PDF_SKIP");

    companion object {
        val DEFAULT_MODE = CREATE

        fun getOption(pdfMode: String): PdfOptions {
            for (option in values())
                if (option.pdfMode.equals(pdfMode, ignoreCase = true))
                    return option

            return DEFAULT_MODE
        }
    }
}
