package ru.kmorozov.gbd.test;

import org.junit.Before;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.config.IGBDOptions;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.output.consumers.DummyBookInfoOutput;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by km on 22.01.2017.
 */
public class GbdTestBase {

    @Before
    public void init() {
        IGBDOptions options = mock(IGBDOptions.class);
        when(options.secureMode()).thenReturn(false);

        ExecutionContext.initContext(new DummyBookInfoOutput(), true);

        GBDOptions.init(options);
    }
}
