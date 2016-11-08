package ru.kmorozov.gbd;

import ru.kmorozov.gbd.core.config.CommandLineOptions;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.config.LocalSystemOptions;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.extractors.ImageExtractor;
import ru.kmorozov.gbd.core.logic.output.consumers.DummyBookInfoOutput;
import ru.kmorozov.gbd.desktop.gui.MainFrame;
import ru.kmorozov.gbd.desktop.output.progress.ProcessStatus;
import ru.kmorozov.gbd.pdf.PdfMaker;

import static ru.kmorozov.gbd.core.logic.context.ExecutionContext.INSTANCE;

class Main {

    public static void main(String[] args) {
        if (args.length > 0) {

            GBDOptions.init(new CommandLineOptions(args));

            String bookId = GBDOptions.getBookId();
            if (bookId == null || bookId.length() == 0) return;

            ExecutionContext.initContext(new DummyBookInfoOutput(), true);
            BookContext bookContext = INSTANCE.getBookContext(GBDOptions.getBookId());

            long pagesProcessed = (new ImageExtractor(bookContext, new ProcessStatus())).process();
            PdfMaker pdfMaker = new PdfMaker(bookContext.getOutputDir(), bookContext.getBookInfo());
            pdfMaker.make(pagesProcessed > 0);
        } else {
            GBDOptions.init(new LocalSystemOptions());
            (new MainFrame()).setVisible();
        }
    }
}