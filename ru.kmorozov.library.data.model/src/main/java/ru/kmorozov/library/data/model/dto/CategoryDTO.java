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
        this.id = category.getId();
        this.name = category.getName();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
