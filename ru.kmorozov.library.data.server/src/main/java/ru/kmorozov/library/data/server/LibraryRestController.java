package ru.kmorozov.library.data.server;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kmorozov.gbd.client.IRestClient;

/**
 * Created by km on 19.12.2016.
 */

@RestController
public class LibraryRestController implements IRestClient {

    @RequestMapping("/ping")
    @Override
    public boolean ping() {
        return true;
    }
}
