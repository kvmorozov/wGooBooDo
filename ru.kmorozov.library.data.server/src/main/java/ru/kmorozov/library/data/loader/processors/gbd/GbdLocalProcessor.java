package ru.kmorozov.library.data.loader.processors.gbd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.loader.ListBasedContextLoader;
import ru.kmorozov.gbd.core.logic.context.ContextProvider;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.context.IBookListProducer;
import ru.kmorozov.gbd.core.producers.SingleBookProducer;
import ru.kmorozov.gbd.logger.output.DummyReceiver;
import ru.kmorozov.library.data.loader.processors.IGbdProcessor;
import ru.kmorozov.library.data.server.options.LocalServerGBDOptions;

@Component
public class GbdLocalProcessor implements IGbdProcessor {

    @Autowired
    private LocalServerGBDOptions options;

    @Override
    public void load(String bookId) {
        options.setBookId(bookId);
        GBDOptions.init(options);

        IBookListProducer producer = new SingleBookProducer(bookId);
        ContextProvider.setDefaultContextProvider(new ListBasedContextLoader(producer));

        ExecutionContext.initContext(new DummyReceiver(), 1 == producer.getBookIds().size());
        ExecutionContext.INSTANCE.addBookContext(producer, new DummyProgress(), new ServerPdfMaker());

        ExecutionContext.INSTANCE.execute();
    }

    @Override
    public void process() {

    }
}
