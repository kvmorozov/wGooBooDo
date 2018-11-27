package ru.kmorozov.gbd.core.config.options;

public enum PdfOptions {
    CREATE("PDF_CREATE"),
    SKIP("PDF_SKIP");

    private final String pdfMode;
    public static final PdfOptions DEFAULT_MODE = PdfOptions.CREATE;

    PdfOptions(String pdfMode) {
        this.pdfMode = pdfMode;
    }

    public static PdfOptions getOption(String pdfMode) {
        for(PdfOptions option : PdfOptions.values())
            if (option.pdfMode.equalsIgnoreCase(pdfMode))
                return option;

        return PdfOptions.DEFAULT_MODE;
    }
}
