package ru.kmorozov.gbd;

import ru.kmorozov.gbd.core.config.CommandLineOptions;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.config.LocalSystemOptions;
import ru.kmorozov.gbd.core.logic.ExecutionContext;
import ru.kmorozov.gbd.core.logic.extractors.ImageExtractor;
import ru.kmorozov.gbd.core.logic.output.consumers.DummyBookInfoOutput;
import ru.kmorozov.gbd.desktop.gui.MainFrame;
import ru.kmorozov.gbd.desktop.output.progress.ProcessStatus;
import ru.kmorozov.gbd.pdf.PdfMaker;

import static ru.kmorozov.gbd.core.logic.ExecutionContext.INSTANCE;
import static ru.kmorozov.gbd.core.logic.extractors.ImageExtractor.BOOK_ID_PLACEHOLDER;
import static ru.kmorozov.gbd.core.logic.extractors.ImageExtractor.HTTPS_TEMPLATE;

class Main {

    public static void main(String[] args) {
        if (args.length > 0) {

            GBDOptions.init(new CommandLineOptions(args));

            String bookId = GBDOptions.getBookId();
            if (bookId == null || bookId.length() == 0) return;

            INSTANCE.setBookId(GBDOptions.getBookId());
            INSTANCE.setBaseUrl(HTTPS_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, INSTANCE.getBookId()));

            ExecutionContext.INSTANCE.setOutput(new DummyBookInfoOutput());
            long pagesProcessed = (new ImageExtractor(new ProcessStatus())).process();
            PdfMaker pdfMaker = new PdfMaker(INSTANCE.getOutputDir(), INSTANCE.getBookInfo());
            pdfMaker.make(pagesProcessed > 0);
        } else {
            GBDOptions.init(new LocalSystemOptions());
            (new MainFrame()).setVisible();
        }
    }
}