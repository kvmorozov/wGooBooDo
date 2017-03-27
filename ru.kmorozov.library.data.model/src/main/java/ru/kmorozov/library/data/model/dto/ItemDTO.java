package ru.kmorozov.library.data.model.dto;

import org.springframework.hateoas.ResourceSupport;

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

    public ItemDTO() {}

    public ItemDTO(StorageDTO storageDTO) {
        this.itemId = storageDTO.getId();
        this.itemType = ItemType.storage;
        this.itemSubType = storageDTO.getStorageType();
        this.displayName = storageDTO.getDisplayName();
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

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public Object getItemSubType() {
        return itemSubType;
    }

    public void setItemSubType(Object itemSubType) {
        this.itemSubType = itemSubType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
