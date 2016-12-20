package ru.kmorozov.gbd.client.test;

import org.junit.Before;
import org.junit.Test;
import ru.kmorozov.gbd.client.RestClient;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.config.IGBDOptions;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by km on 20.12.2016.
 */
public class RestClientTest {

    @Before
    public void init() {
        IGBDOptions options = mock(IGBDOptions.class);
        when(options.secureMode()).thenReturn(false);

        GBDOptions.init(options);
    }

    @Test
    public void connectTest() {
        RestClient restClient = new RestClient();
        assertTrue(restClient.serviceAvailable());
    }
}
