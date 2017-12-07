package ru.kmorozov.gbd.test;

import org.junit.Before;
import org.mockito.Mockito;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.config.IGBDOptions;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.output.consumers.DummyBookInfoOutput;

/**
 * Created by km on 22.01.2017.
 */
public class GbdTestBase {

    @Before
    public void init() {
        IGBDOptions options = Mockito.mock(IGBDOptions.class);
        Mockito.when(options.secureMode()).thenReturn(false);

        ExecutionContext.initContext(new DummyBookInfoOutput(), true);

        GBDOptions.init(options);
    }
}
