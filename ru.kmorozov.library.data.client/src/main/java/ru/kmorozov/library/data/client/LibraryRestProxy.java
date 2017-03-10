package ru.kmorozov.library.data.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.kmorozov.library.data.model.IDataRestServer;
import ru.kmorozov.library.data.model.dto.BookDTO;
import ru.kmorozov.library.data.model.dto.ItemDTO;
import ru.kmorozov.library.data.model.dto.StorageDTO;
import ru.kmorozov.library.data.model.dto.UserDTO;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * Created by sbt-morozov-kv on 19.01.2017.
 */

@RestController
public class LibraryRestProxy implements IDataRestServer {

    private final RestTemplate template;

    public LibraryRestProxy(RestTemplate template) {
        this.template = template;
    }

    @Override
    @RequestMapping("/login")
    public UserDTO login(@RequestParam(name = "login") String login) {
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        String uri = builder.scheme("http")
                .host("localhost")
                .port(9000)
                .path("login")
                .queryParam("login", login)
                .build().toString();

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
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        String uri = builder.scheme("http")
                .host("localhost")
                .port(9000)
                .path("itemByIdAndType")
                .queryParam("itemId", itemId)
                .queryParam("itemType", itemType.toString())
                .build().toString();

        return template.getForEntity(uri, ItemDTO.class).getBody();
    }

    private List getList(String operation, String paramName, String paramValue, Class arrClass) {
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        String uri = builder.scheme("http")
                .host("localhost")
                .port(9000)
                .path(operation)
                .queryParam(paramName, paramValue)
                .build().toString();
        return Arrays.stream((Object[]) template.getForEntity(uri, arrClass).getBody()).collect(Collectors.toList());
    }
}
