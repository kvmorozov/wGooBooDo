package ru.kmorozov.library.data.client;

import org.apache.hc.client5.http.utils.URIBuilder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ru.kmorozov.library.data.model.IDataRestServer;
import ru.kmorozov.library.data.model.dto.BookDTO;
import ru.kmorozov.library.data.model.dto.ItemDTO;
import ru.kmorozov.library.data.model.dto.StorageDTO;
import ru.kmorozov.library.data.model.dto.UserDTO;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * Created by sbt-morozov-kv on 19.01.2017.
 */

@RestController
public class LibraryRestProxy implements IDataRestServer {

    private static final RestTemplate template = new RestTemplate();

    @Override
    @RequestMapping("/login")
    public UserDTO login(@RequestParam(name = "login") String login) {
        URIBuilder builder = new URIBuilder();
        String uri = null;

        try {
            uri = builder.setScheme("http")
                    .setHost("localhost")
                    .setPort(9000)
                    .setPath("login")
                    .setParameter("login", login)
                    .build().toString();
        } catch (URISyntaxException e) {
            return null;
        }

        UserDTO user = template.getForEntity(uri, UserDTO.class).getBody();
        user.add(linkTo(methodOn(LibraryRestProxy.class).getItemsByStorageId("root")).withRel("root"));

        return user;
    }

    @Override
    @RequestMapping("/storages/{storageId}")
    public List<StorageDTO> getStoragesByParentId(@PathVariable String storageId) {
        return getList("/storagesByParentId", "storageId", storageId, StorageDTO[].class);
    }

    @Override
    @RequestMapping("/books/{storageId}")
    public List<BookDTO> getBooksByStorageId(@PathVariable String storageId) {
        return getList("/booksByStorageId", "storageId", storageId, BookDTO[].class);
    }

    @Override
    @RequestMapping("/items/{storageId}")
    public List<ItemDTO> getItemsByStorageId(@PathVariable String storageId) {
        List<ItemDTO> result = getList("/itemsByStorageId", "storageId", storageId, ItemDTO[].class);

        return result
                .stream()
                .map(item -> {
                    item.add(linkTo(methodOn(LibraryRestProxy.class, item.getItemType(), item.getItemId())
                            .itemByIdAndType(item.getItemId(), item.getItemType())).withSelfRel());

                    switch (item.getItemType()) {
                        case storage:
                            item.add(linkTo(methodOn(LibraryRestProxy.class)
                                    .getBooksByStorageId(item.getItemId())).withRel("books"));
                            item.add(linkTo(methodOn(LibraryRestProxy.class)
                                    .getStoragesByParentId(item.getItemId())).withRel("storages"));
                            item.add(linkTo(methodOn(LibraryRestProxy.class)
                                    .getItemsByStorageId(item.getItemId())).withRel("items"));
                            break;
                        default:
                    }
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Override
    @RequestMapping("/item/{itemType}/{itemId}")
    public ItemDTO itemByIdAndType(@PathVariable String itemId, @PathVariable ItemDTO.ItemType itemType) {
        URIBuilder builder = new URIBuilder();
        String uri = null;

        try {
            uri = builder.setScheme("http")
                    .setHost("localhost")
                    .setPort(9000)
                    .setPath("itemByIdAndType")
                    .setParameter("itemId", itemId)
                    .setParameter("itemType", itemType.toString())
                    .build().toString();
        } catch (URISyntaxException e) {
            return null;
        }

        return template.getForEntity(uri, ItemDTO.class).getBody();
    }

    private List getList(String operation, String paramName, String paramValue, Class arrClass) {
        URIBuilder builder = new URIBuilder();
        String uri = null;

        try {
            uri = builder.setScheme("http")
                    .setHost("localhost")
                    .setPort(9000)
                    .setPath(operation)
                    .setParameter(paramName, paramValue)
                    .build().toString();
        } catch (URISyntaxException e) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.stream((Object[]) template.getForEntity(uri, arrClass).getBody()).collect(Collectors.toList());
    }
}
