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
    private ItemDTO.ItemType itemType;
    private Object itemSubType;
    private String displayName;
    private long filesCount;
    private List<CategoryDTO> categories;
    private ItemDTO.RefreshStatus refreshStatus;

    public ItemDTO() {
    }

    public ItemDTO(StorageDTO storageDTO) {
        itemId = storageDTO.getId();
        itemType = ItemDTO.ItemType.storage;
        itemSubType = storageDTO.getStorageType();
        displayName = storageDTO.getDisplayName();
        filesCount = storageDTO.getFilesCount();

        this.refreshStatus = (long) REFRESH_INTERVAL > System.currentTimeMillis() - storageDTO.getLastChecked() ? ItemDTO.RefreshStatus.updated : ItemDTO.RefreshStatus.dirty;

        if (null != storageDTO.getCategories())
            categories = storageDTO.getCategories().stream().map(CategoryDTO::new).collect(Collectors.toList());
    }

    public ItemDTO(BookDTO bookDTO) {
        itemId = bookDTO.getId();
        itemType = ItemDTO.ItemType.book;
        itemSubType = bookDTO.getFormat();
        displayName = bookDTO.getTitle();
    }

    public String getItemId() {
        return this.itemId;
    }

    public ItemDTO.ItemType getItemType() {
        return this.itemType;
    }

    public Object getItemSubType() {
        return this.itemSubType;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public long getFilesCount() {
        return this.filesCount;
    }

    public List<CategoryDTO> getCategories() {
        return this.categories;
    }

    public ItemDTO.RefreshStatus getRefreshStatus() {
        return this.refreshStatus;
    }

    public void setUpdated() {
        refreshStatus = ItemDTO.RefreshStatus.updated;
    }
}
