package ru.kmorozov.gbd;

import ru.kmorozov.gbd.core.config.CommandLineOptions;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.config.LocalSystemOptions;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.context.IBookListProducer;
import ru.kmorozov.gbd.core.logic.output.consumers.DummyBookInfoOutput;
import ru.kmorozov.gbd.desktop.gui.MainFrame;
import ru.kmorozov.gbd.desktop.library.OptionsBasedProducer;
import ru.kmorozov.gbd.desktop.output.progress.ProcessStatus;
import ru.kmorozov.gbd.pdf.PdfMaker;

class Main {

    public static void main(String[] args) {
        if (args.length > 0) {
            GBDOptions.init(new CommandLineOptions(args));

            IBookListProducer producer = new OptionsBasedProducer();
            ExecutionContext.initContext(new DummyBookInfoOutput(), producer.getBookIds().size() == 1);
            ExecutionContext.INSTANCE.addBookContext(producer, new ProcessStatus(), new PdfMaker());

            ExecutionContext.INSTANCE.execute();
        }
        else {
            GBDOptions.init(new LocalSystemOptions());
            (new MainFrame()).setVisible();
        }
    }
}