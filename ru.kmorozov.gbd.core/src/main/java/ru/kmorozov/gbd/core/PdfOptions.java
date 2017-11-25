package ru.kmorozov.gbd.core;

public enum PdfOptions {
    CREATE("PDF_CREATE"),
    SKIP("PDF_SKIP");

    String pdfMode;
    public static PdfOptions DEFAULT_MODE = CREATE;

    PdfOptions(String pdfMode) {
        this.pdfMode = pdfMode;
    }

    public static PdfOptions getOption(String pdfMode) {
        for(PdfOptions option : PdfOptions.values())
            if (option.pdfMode.equalsIgnoreCase(pdfMode))
                return option;

        return DEFAULT_MODE;
    }
}
