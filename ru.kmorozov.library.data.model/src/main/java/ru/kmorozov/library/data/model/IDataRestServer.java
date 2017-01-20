package ru.kmorozov.library.data.model;

import ru.kmorozov.library.data.model.dto.StorageDTO;

import java.util.List;

/**
 * Created by sbt-morozov-kv on 17.01.2017.
 */
public interface IDataRestServer {

    List<StorageDTO> getStoragesByParentId(String storageId);
}
