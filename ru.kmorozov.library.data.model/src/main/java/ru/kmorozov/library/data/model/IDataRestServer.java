package ru.kmorozov.library.data.model;

import ru.kmorozov.library.data.model.dto.*;
import ru.kmorozov.library.data.model.dto.ItemDTO.ItemType;

import java.util.List;

/**
 * Created by sbt-morozov-kv on 17.01.2017.
 */
public interface IDataRestServer {

    UserDTO login(String login);

    List<StorageDTO> getStoragesByParentId(String storageId);

    List<BookDTO> getBooksByStorageId(String storageId);

    List<ItemDTO> getItemsByStorageId(String storageId);

    ItemDTO getItem(String itemId, ItemType itemType, boolean refresh);

    void updateLibrary(String state);

    BookDTO downloadBook(String bookId);

    List<DuplicatedBookDTO> findDuplicates();

    void synchronizeDb();
}
