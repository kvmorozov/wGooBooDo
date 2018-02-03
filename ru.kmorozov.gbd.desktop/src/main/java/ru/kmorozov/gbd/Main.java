package ru.kmorozov.gbd;

import ru.kmorozov.gbd.core.config.CommandLineOptions;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.config.LocalSystemOptions;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.context.IBookListProducer;
import ru.kmorozov.gbd.desktop.gui.MainFrame;
import ru.kmorozov.gbd.desktop.library.OptionsBasedProducer;
import ru.kmorozov.gbd.desktop.output.progress.ProcessStatus;
import ru.kmorozov.gbd.logger.output.DummyReceiver;
import ru.kmorozov.gbd.pdf.PdfMaker;

class Main {

    public static void main(final String[] args) {
        if (0 < args.length) {
            GBDOptions.init(new CommandLineOptions(args));

            final IBookListProducer producer = new OptionsBasedProducer();
            ExecutionContext.initContext(new DummyReceiver(), 1 == producer.getBookIds().size());
            ExecutionContext.INSTANCE.addBookContext(producer, new ProcessStatus(), new PdfMaker());

            ExecutionContext.INSTANCE.execute();
        }
        else {
            GBDOptions.init(new LocalSystemOptions());
            (new MainFrame()).setVisible();
        }
    }
}