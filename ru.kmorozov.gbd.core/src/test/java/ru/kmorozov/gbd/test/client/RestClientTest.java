package ru.kmorozov.gbd.test.client;

import org.junit.Test;
import ru.kmorozov.gbd.client.RestClient;
import ru.kmorozov.gbd.test.GbdTestBase;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by km on 20.12.2016.
 */
public class RestClientTest extends GbdTestBase {

    @Test
    public void connectTest() {
        RestClient restClient = new RestClient();
        assertTrue(restClient.serviceAvailable());
    }
}
