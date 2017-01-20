package ru.kmorozov.library.data.client;

import org.apache.hc.client5.http.utils.URIBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ru.kmorozov.library.data.model.IDataRestServer;
import ru.kmorozov.library.data.model.dto.StorageDTO;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

/**
 * Created by sbt-morozov-kv on 19.01.2017.
 */

@RestController
public class LibraryRestProxy implements IDataRestServer {

    private static final RestTemplate template = new RestTemplate();

    @Override
    @RequestMapping("/storagesByParentId")
    public List<StorageDTO> getStoragesByParentId(@RequestParam(name = "storageId") String storageId) {
        URIBuilder builder = new URIBuilder();
        String uri = null;

        try {
            uri = builder.setScheme("http")
                    .setHost("localhost")
                    .setPort(9000)
                    .setPath("/storagesByParentId")
                    .setParameter("storageId", storageId)
                    .build().toString();
        } catch (URISyntaxException e) {
            return Collections.EMPTY_LIST;
        }

        return template.getForEntity(uri, List.class).getBody();
    }
}
