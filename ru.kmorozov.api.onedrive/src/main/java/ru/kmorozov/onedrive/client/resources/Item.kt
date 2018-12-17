package ru.kmorozov.onedrive.client.resources

import com.google.api.client.util.Key
import ru.kmorozov.onedrive.client.facets.DeletedFacet
import ru.kmorozov.onedrive.client.facets.FileFacet
import ru.kmorozov.onedrive.client.facets.FileSystemInfoFacet
import ru.kmorozov.onedrive.client.facets.FolderFacet

class Item {

    @Key
    val id: String? = null
    @Key
    val name: String? = null
    @Key
    private val eTag: String? = null
    @Key
    private val cTag: String? = null
    @Key
    val createdBy: IdentitySet? = null
    @Key
    val lastModifiedBy: IdentitySet? = null
    @Key
    val createdDateTime: String? = null
    @Key
    val lastModifiedDateTime: String? = null
    @Key
    val size: Long = 0
    @Key
    val parentReference: ItemReference? = null
    @Key
    val children: Array<Item>? = null
    @Key
    val webUrl: String? = null
    @Key
    val folder: FolderFacet? = null
    @Key
    val file: FileFacet? = null
    @Key
    val fileSystemInfo: FileSystemInfoFacet? = null
    @Key
    val deleted: DeletedFacet? = null

    fun geteTag(): String? {
        return eTag
    }

    fun getcTag(): String? {
        return cTag
    }
}
