package ru.kmorozov.onedrive.client.resources;

import com.google.api.client.util.Key;
import ru.kmorozov.onedrive.client.facets.DeletedFacet;
import ru.kmorozov.onedrive.client.facets.FileFacet;
import ru.kmorozov.onedrive.client.facets.FileSystemInfoFacet;
import ru.kmorozov.onedrive.client.facets.FolderFacet;

public class Item {

    @Key
    private String id;
    @Key
    private String name;
    @Key
    private String eTag;
    @Key
    private String cTag;
    @Key
    private IdentitySet createdBy;
    @Key
    private IdentitySet lastModifiedBy;
    @Key
    private String createdDateTime;
    @Key
    private String lastModifiedDateTime;
    @Key
    private long size;
    @Key
    private ItemReference parentReference;
    @Key
    private Item[] children;
    @Key
    private String webUrl;
    @Key
    private FolderFacet folder;
    @Key
    private FileFacet file;
    @Key
    private FileSystemInfoFacet fileSystemInfo;
    @Key
    private DeletedFacet deleted;

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String geteTag() {
        return this.eTag;
    }

    public String getcTag() {
        return this.cTag;
    }

    public IdentitySet getCreatedBy() {
        return this.createdBy;
    }

    public IdentitySet getLastModifiedBy() {
        return this.lastModifiedBy;
    }

    public String getCreatedDateTime() {
        return this.createdDateTime;
    }

    public String getLastModifiedDateTime() {
        return this.lastModifiedDateTime;
    }

    public long getSize() {
        return this.size;
    }

    public ItemReference getParentReference() {
        return this.parentReference;
    }

    public String getWebUrl() {
        return this.webUrl;
    }

    public FolderFacet getFolder() {
        return this.folder;
    }

    public FileFacet getFile() {
        return this.file;
    }

    public FileSystemInfoFacet getFileSystemInfo() {
        return this.fileSystemInfo;
    }

    public DeletedFacet getDeleted() {
        return this.deleted;
    }

    public Item[] getChildren() {
        return this.children;
    }
}
