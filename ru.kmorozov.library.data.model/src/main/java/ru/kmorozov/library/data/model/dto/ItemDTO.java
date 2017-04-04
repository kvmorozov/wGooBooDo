package ru.kmorozov.library.data.model.dto;

import org.springframework.hateoas.ResourceSupport;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sbt-morozov-kv on 31.01.2017.
 */
public class ItemDTO extends ResourceSupport {

    public enum ItemType {
        storage,
        category,
        book
    }

    private String itemId;
    private ItemType itemType;
    private Object itemSubType;
    private String displayName;
    private long filesCount;
    private List<CategoryDTO> categories;

    public ItemDTO() {
    }

    public ItemDTO(StorageDTO storageDTO) {
        this.itemId = storageDTO.getId();
        this.itemType = ItemType.storage;
        this.itemSubType = storageDTO.getStorageType();
        this.displayName = storageDTO.getDisplayName();
        this.filesCount = storageDTO.getFilesCount();

        if (storageDTO.getCategories() != null)
            this.categories = storageDTO.getCategories().stream().map(CategoryDTO::new).collect(Collectors.toList());
    }

    public ItemDTO(BookDTO bookDTO) {
        this.itemId = bookDTO.getId();
        this.itemType = ItemType.book;
        this.itemSubType = bookDTO.getFormat();
        this.displayName = bookDTO.getTitle();
    }

    public String getItemId() {
        return itemId;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public Object getItemSubType() {
        return itemSubType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getFilesCount() {
        return filesCount;
    }

    public List<CategoryDTO> getCategories() {
        return categories;
    }
}
