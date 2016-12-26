package ru.kmorozov.library.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.kmorozov.library.data.model.book.Storage;

/**
 * Created by km on 26.12.2016.
 */
public interface StorageRepository extends MongoRepository<Storage, String> {

    Storage findByUrl(String url);
}
