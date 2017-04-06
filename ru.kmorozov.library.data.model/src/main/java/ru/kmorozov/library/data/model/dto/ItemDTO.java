package ru.kmorozov.library.data.model.dto;

import org.springframework.hateoas.ResourceSupport;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sbt-morozov-kv on 31.01.2017.
 */
public class ItemDTO extends ResourceSupport {

    public static final int REFRESH_INTERVAL = 10 * 1000 * 60;

    public enum ItemType {
        storage,
        category,
        book
    }

    public enum RefreshStatus {
        dirty,
        updated
    }

    private String itemId;
    private ItemType itemType;
    private Object itemSubType;
    private String displayName;
    private long filesCount;
    private List<CategoryDTO> categories;
    private RefreshStatus refreshStatus;

    public ItemDTO() {
    }

    public ItemDTO(StorageDTO storageDTO) {
        this.itemId = storageDTO.getId();
        this.itemType = ItemType.storage;
        this.itemSubType = storageDTO.getStorageType();
        this.displayName = storageDTO.getDisplayName();
        this.filesCount = storageDTO.getFilesCount();

        refreshStatus = System.currentTimeMillis() - storageDTO.getLastChecked() < ItemDTO.REFRESH_INTERVAL ? RefreshStatus.updated : RefreshStatus.dirty;

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

    public RefreshStatus getRefreshStatus() {
        return refreshStatus;
    }

    public void setUpdated() {
        this.refreshStatus = RefreshStatus.updated;
    }
}
