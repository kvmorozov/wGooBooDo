package ru.kmorozov.library.data.model;

import ru.kmorozov.library.data.model.dto.BookDTO;
import ru.kmorozov.library.data.model.dto.ItemDTO;
import ru.kmorozov.library.data.model.dto.StorageDTO;
import ru.kmorozov.library.data.model.dto.UserDTO;

import java.util.List;

/**
 * Created by sbt-morozov-kv on 17.01.2017.
 */
public interface IDataRestServer {

    UserDTO login(String login);

    List<StorageDTO> getStoragesByParentId(String storageId);

    List<BookDTO> getBooksByStorageId(String storageId);

    List<ItemDTO> getItemsByStorageId(String storageId);

    ItemDTO itemByIdAndType(String itemId, ItemDTO.ItemType itemType);

    void updateLibrary();
}
