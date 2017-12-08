package ru.kmorozov.gbd.core;

public enum PdfOptions {
    CREATE("PDF_CREATE"),
    SKIP("PDF_SKIP");

    private final String pdfMode;
    public static final PdfOptions DEFAULT_MODE = CREATE;

    PdfOptions(final String pdfMode) {
        this.pdfMode = pdfMode;
    }

    public static PdfOptions getOption(final String pdfMode) {
        for(final PdfOptions option : PdfOptions.values())
            if (option.pdfMode.equalsIgnoreCase(pdfMode))
                return option;

        return DEFAULT_MODE;
    }
}
