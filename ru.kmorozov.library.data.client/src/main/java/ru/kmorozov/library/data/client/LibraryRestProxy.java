package ru.kmorozov.library.data.client;

import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.kmorozov.library.data.model.IDataRestServer;
import ru.kmorozov.library.data.model.dto.*;
import ru.kmorozov.library.data.model.dto.ItemDTO.ItemType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sbt-morozov-kv on 19.01.2017.
 */

@RestController
public class LibraryRestProxy implements IDataRestServer {

    private final RestTemplate template;

    public LibraryRestProxy(final RestTemplate template) {
        this.template = template;
    }

    @Override
    @RequestMapping("/login")
    public UserDTO login(@RequestParam(name = "login") final String login) {
        final UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        final String uri = builder.scheme("http")
                .host("localhost")
                .port(9000)
                .path("login")
                .queryParam("login", login)
                .build().toString();

        final UserDTO user = template.getForEntity(uri, UserDTO.class).getBody();
        user.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(LibraryRestProxy.class).getItemsByStorageId("root")).withRel("root"));

        return user;
    }

    @Override
    @RequestMapping("/storages/{storageId}")
    public List<StorageDTO> getStoragesByParentId(@PathVariable final String storageId) {
        return getList("/storagesByParentId", "storageId", storageId, StorageDTO[].class);
    }

    @Override
    @RequestMapping("/books/{storageId}")
    public List<BookDTO> getBooksByStorageId(@PathVariable final String storageId) {
        return getList("/booksByStorageId", "storageId", storageId, BookDTO[].class);
    }

    @Override
    @RequestMapping("/items/{storageId}")
    public List<ItemDTO> getItemsByStorageId(@PathVariable final String storageId) {
        final List<ItemDTO> result = getList("/itemsByStorageId", "storageId", storageId, ItemDTO[].class);

        return result
                .stream()
                .map(LibraryRestProxy::addLinks)
                .collect(Collectors.toList());
    }

    private static ItemDTO addLinks(final ItemDTO item) {
        item.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(LibraryRestProxy.class, item.getItemType(), item.getItemId())
                                                                   .getItem(item.getItemId(), item.getItemType(), false)).withSelfRel());
        item.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(LibraryRestProxy.class)
                                                                   .getItem(item.getItemId(), item.getItemType(), true)).withRel("refresh"));

        switch (item.getItemType()) {
            case storage:
                item.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(LibraryRestProxy.class)
                                                                           .getBooksByStorageId(item.getItemId())).withRel("books"));
                item.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(LibraryRestProxy.class)
                                                                           .getStoragesByParentId(item.getItemId())).withRel("storages"));
                item.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(LibraryRestProxy.class)
                                                                           .getItemsByStorageId(item.getItemId())).withRel("items"));
                break;
            default:
        }

        return item;
    }

    @Override
    @RequestMapping("/item/{itemType}/{itemId}/{refresh}")
    public ItemDTO getItem(@PathVariable(name = "itemId") final String itemId,
                           @PathVariable(name = "itemType") final ItemType itemType,
                           @PathVariable(name = "refresh") final boolean refresh) {
        final UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        final String uri = builder.scheme("http")
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
    public void updateLibrary(@PathVariable final String state) {
        final UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        final String uri = builder.scheme("http")
                .host("localhost")
                .port(9000)
                .path("updateLibrary")
                .queryParam("state", state)
                .build().toString();

        template.execute(uri, HttpMethod.POST, null, null);
    }

    @Override
    @RequestMapping("/downloadBook/{bookId}")
    public BookDTO downloadBook(@PathVariable final String bookId) {
        final UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        final String uri = builder.scheme("http")
                .host("localhost")
                .port(9000)
                .path("downloadBook")
                .queryParam("bookId", bookId)
                .build().toString();

        return template.getForEntity(uri, BookDTO.class).getBody();
    }

    private List getList(final String operation, final String paramName, final String paramValue, final Class arrClass) {
        final UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        builder.scheme("http").host("localhost").port(9000).path(operation);

        if (!StringUtils.isEmpty(paramName) && !StringUtils.isEmpty(paramValue))
            builder.queryParam(paramName, paramValue);

        final String uri = builder.build().toString();

        return Arrays.stream((Object[]) template.getForEntity(uri, arrClass).getBody()).collect(Collectors.toList());
    }

    @Override
    @RequestMapping("/findDuplicates")
    public List<DuplicatedBookDTO> findDuplicates() {
        return getList("/findDuplicates", null, null, DuplicatedBookDTO[].class);
    }
}
