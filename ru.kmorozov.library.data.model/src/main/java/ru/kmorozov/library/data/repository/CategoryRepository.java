package ru.kmorozov.library.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.kmorozov.library.data.model.book.Category;

/**
 * Created by km on 26.12.2016.
 */
public interface CategoryRepository extends MongoRepository<Category, String> {

    Category findOneByName(String name);
}
