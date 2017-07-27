package ru.kmorozov.library.data.client;

import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.kmorozov.library.data.model.IDataRestServer;
import ru.kmorozov.library.data.model.dto.*;

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
                .map(this::addLinks)
                .collect(Collectors.toList());
    }

    private ItemDTO addLinks(ItemDTO item) {
        item.add(linkTo(methodOn(LibraryRestProxy.class, item.getItemType(), item.getItemId())
                .getItem(item.getItemId(), item.getItemType(), false)).withSelfRel());
        item.add(linkTo(methodOn(LibraryRestProxy.class)
                .getItem(item.getItemId(), item.getItemType(), true)).withRel("refresh"));

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
    }

    @Override
    @RequestMapping("/item/{itemType}/{itemId}/{refresh}")
    public ItemDTO getItem(@PathVariable(name = "itemId") String itemId,
                           @PathVariable(name = "itemType") ItemDTO.ItemType itemType,
                           @PathVariable(name = "refresh") boolean refresh) {
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        String uri = builder.scheme("http")
                .host("localhost")
                .port(9000)
                .path("item")
                .queryParam("itemId", itemId)
                .queryParam("itemType", itemType.toString())
                .queryParam("refresh", refresh)
                .build().toString();

        return addLinks(template.getForEntity(uri, ItemDTO.class).getBody());
    }

    @Override
    @RequestMapping(value = "/updateLibrary/{state}", method = RequestMethod.POST)
    public void updateLibrary(@PathVariable String state) {
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        String uri = builder.scheme("http")
                .host("localhost")
                .port(9000)
                .path("updateLibrary")
                .queryParam("state", state)
                .build().toString();

        template.execute(uri, HttpMethod.POST, null, null);
    }

    @Override
    @RequestMapping("/downloadBook/{bookId}")
    public BookDTO downloadBook(@PathVariable String bookId) {
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        String uri = builder.scheme("http")
                .host("localhost")
                .port(9000)
                .path("downloadBook")
                .queryParam("bookId", bookId)
                .build().toString();

        BookDTO book = template.getForEntity(uri, BookDTO.class).getBody();
        return book;
    }

    private List getList(String operation, String paramName, String paramValue, Class arrClass) {
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        builder.scheme("http").host("localhost").port(9000).path(operation);

        if (!StringUtils.isEmpty(paramName) && !StringUtils.isEmpty(paramValue))
            builder.queryParam(paramName, paramValue);

        String uri = builder.build().toString();

        return Arrays.stream((Object[]) template.getForEntity(uri, arrClass).getBody()).collect(Collectors.toList());
    }

    @Override
    @RequestMapping("/findDuplicates")
    public List<DuplicatedBookDTO> findDuplicates() {
        return getList("/findDuplicates", null, null, DuplicatedBookDTO[].class);
    }
}
