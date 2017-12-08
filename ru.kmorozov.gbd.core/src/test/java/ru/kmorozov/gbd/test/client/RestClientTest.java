package ru.kmorozov.gbd.test.client;

import org.junit.Assert;
import org.junit.Test;
import ru.kmorozov.gbd.client.RestClient;
import ru.kmorozov.gbd.test.GbdTestBase;

/**
 * Created by km on 20.12.2016.
 */
public class RestClientTest extends GbdTestBase {

    @Test
    public void connectTest() {
        final RestClient restClient = new RestClient();
        Assert.assertTrue(restClient.serviceAvailable());
    }
}
