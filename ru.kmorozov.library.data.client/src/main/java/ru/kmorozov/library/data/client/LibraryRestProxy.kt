package ru.kmorozov.library.data.client

import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.http.HttpMethod
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import ru.kmorozov.library.data.model.IDataRestServer
import ru.kmorozov.library.data.model.dto.*
import ru.kmorozov.library.data.model.dto.ItemDTO.ItemType
import java.util.*
import java.util.stream.Collectors

/**
 * Created by sbt-morozov-kv on 19.01.2017.
 */

@RestController
class LibraryRestProxy(private val template: RestTemplate) : IDataRestServer {

    @RequestMapping("/login")
    override fun login(@RequestParam(name = "login") login: String): UserDTO {
        val builder = UriComponentsBuilder.newInstance()
        val uri = builder.scheme("http")
                .host("localhost")
                .port(9000)
                .path("login")
                .queryParam("login", login)
                .build().toString()

        val user = template.getForEntity(uri, UserDTO::class.java).body
        user!!.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(LibraryRestProxy::class.java).getItemsByStorageId("root")).withRel("root"))

        return user
    }

    @RequestMapping("/storages/{storageId}")
    override fun getStoragesByParentId(@PathVariable storageId: String): List<StorageDTO> {
        return getList("/storagesByParentId", "storageId", storageId, Array<StorageDTO>::class.java) as List<StorageDTO>
    }

    @RequestMapping("/books/{storageId}")
    override fun getBooksByStorageId(@PathVariable storageId: String): List<BookDTO> {
        return getList("/booksByStorageId", "storageId", storageId, Array<BookDTO>::class.java) as List<BookDTO>
    }

    @RequestMapping("/items/{storageId}")
    override fun getItemsByStorageId(@PathVariable storageId: String): List<ItemDTO> {
        val result = getList("/itemsByStorageId", "storageId", storageId, Array<ItemDTO>::class.java) as List<ItemDTO>

        return result
                .stream()
                .map<ItemDTO> { addLinks(it) }
                .collect(Collectors.toList())
    }

    private fun addLinks(item: ItemDTO): ItemDTO {
        item.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(LibraryRestProxy::class.java, item.itemType, item.itemId)
                .getItem(item.itemId!!, item.itemType!!, false)).withSelfRel())
        item.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(LibraryRestProxy::class.java)
                .getItem(item.itemId!!, item.itemType!!, true)).withRel("refresh"))

        when (item.itemType) {
            ItemType.storage -> {
                item.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(LibraryRestProxy::class.java)
                        .getBooksByStorageId(item.itemId!!)).withRel("books"))
                item.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(LibraryRestProxy::class.java)
                        .getStoragesByParentId(item.itemId!!)).withRel("storages"))
                item.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(LibraryRestProxy::class.java)
                        .getItemsByStorageId(item.itemId!!)).withRel("items"))
            }
            ItemType.category -> TODO()
            ItemType.book -> TODO()
            null -> TODO()
        }

        return item
    }

    @RequestMapping("/item/{itemType}/{itemId}/{refresh}")
    override fun getItem(@PathVariable(name = "itemId") itemId: String,
                         @PathVariable(name = "itemType") itemType: ItemType,
                         @PathVariable(name = "refresh") refresh: Boolean): ItemDTO {
        val builder = UriComponentsBuilder.newInstance()
        val uri = builder.scheme("http")
                .host("localhost")
                .port(9000)
                .path("item")
                .queryParam("itemId", itemId)
                .queryParam("itemType", itemType.toString())
                .queryParam("refresh", refresh)
                .build().toString()

        return addLinks(template.getForEntity(uri, ItemDTO::class.java).body!!)
    }

    @RequestMapping(value = ["/updateLibrary/{state}"], method = arrayOf(RequestMethod.POST))
    override fun updateLibrary(@PathVariable state: String) {
        val builder = UriComponentsBuilder.newInstance()
        val uri = builder.scheme("http")
                .host("localhost")
                .port(9000)
                .path("updateLibrary")
                .queryParam("state", state)
                .build().toString()

        template.execute<Any>(uri, HttpMethod.POST, null, null)
    }

    @RequestMapping("/downloadBook/{bookId}")
    override fun downloadBook(@PathVariable bookId: String): BookDTO {
        val builder = UriComponentsBuilder.newInstance()
        val uri = builder.scheme("http")
                .host("localhost")
                .port(9000)
                .path("downloadBook")
                .queryParam("bookId", bookId)
                .build().toString()

        return template.getForEntity(uri, BookDTO::class.java).body!!
    }

    private fun getList(operation: String, paramName: String?, paramValue: String?, arrClass: Class<*>): List<*> {
        val builder = UriComponentsBuilder.newInstance()
        builder.scheme("http").host("localhost").port(9000).path(operation)

        if (!StringUtils.isEmpty(paramName) && !StringUtils.isEmpty(paramValue))
            builder.queryParam(paramName!!, paramValue!!)

        val uri = builder.build().toString()

        return Arrays.stream(template.getForEntity(uri, arrClass).getBody() as Array<Any>).collect(Collectors.toList())
    }

    @RequestMapping("/findDuplicates")
    override fun findDuplicates(): List<DuplicatedBookDTO> {
        return getList("/findDuplicates", null, null, Array<DuplicatedBookDTO>::class.java) as List<DuplicatedBookDTO>
    }

    override fun synchronizeDb() {

    }
}
