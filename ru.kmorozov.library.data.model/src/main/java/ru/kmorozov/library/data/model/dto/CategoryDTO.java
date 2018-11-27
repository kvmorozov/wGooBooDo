package ru.kmorozov.library.data.model.dto;

import ru.kmorozov.library.data.model.book.Category;

/**
 * Created by sbt-morozov-kv on 04.04.2017.
 */
public class CategoryDTO {

    private String id;
    private String name;

    public CategoryDTO() {
    }

    public CategoryDTO(Category category) {
        id = category.getId();
        name = category.getName();
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
}
