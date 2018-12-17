package ru.kmorozov.onedrive.client

import com.google.api.client.util.Throwables
import ru.kmorozov.onedrive.client.facets.FolderFacet
import ru.kmorozov.onedrive.client.resources.Item
import ru.kmorozov.onedrive.client.resources.ItemReference
import ru.kmorozov.onedrive.client.serialization.JsonDateSerializer

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.text.ParseException
import java.util.Date

interface OneDriveItem {
    val id: String?

    val isDirectory: Boolean

    val name: String

    val fullName: String

    val crc32: Long

    val size: Long

    val createdDateTime: Date

    val lastModifiedDateTime: Date

    val parent: OneDriveItem?

    val folder: FolderFacet?

    object FACTORY {

        fun create(parent: OneDriveItem, name: String, isDirectory: Boolean): OneDriveItem {

            return object : OneDriveItem {
                override val id: String
                    get() = ""

                override val isDirectory: Boolean
                    get() = isDirectory

                override val name: String
                    get() = name

                override val fullName: String
                    get() = parent.fullName + name + if (isDirectory) "/" else ""

                override val crc32: Long
                    get() = 0L

                override val size: Long
                    get() = 0L

                override val createdDateTime: Date
                    get() = Date()

                override val lastModifiedDateTime: Date
                    get() = Date()

                override val parent: OneDriveItem
                    get() = parent

                override val folder: FolderFacet
                    get() = FolderFacet()
            }
        }

        fun create(item: Item): OneDriveItem {
            return object : OneDriveItem {

                override val parent = create(item.parentReference)

                override val id: String
                    get() = item.id!!

                override val isDirectory: Boolean
                    get() = null != item.folder

                override val name: String
                    get() = item.name!!

                override val fullName: String
                    get() = parent.fullName + item.name + if (isDirectory) "/" else ""

                override val crc32: Long
                    get() = item.file!!.hashes!!.crc32

                override val size: Long
                    get() = item.size

                override val createdDateTime: Date
                    get() {
                        try {
                            return JsonDateSerializer.INSTANCE.deserialize(item.fileSystemInfo!!.createdDateTime!!)
                        } catch (e: ParseException) {
                            e.printStackTrace()

                            return Date()
                        }

                    }

                override val lastModifiedDateTime: Date
                    get() {
                        try {
                            return JsonDateSerializer.INSTANCE.deserialize(item.fileSystemInfo!!.lastModifiedDateTime!!)
                        } catch (e: ParseException) {
                            e.printStackTrace()

                            return Date()
                        }

                    }

                override val folder: FolderFacet
                    get() = item.folder!!
            }
        }

        fun create(parent: ItemReference?): OneDriveItem {
            return object : OneDriveItem {
                override val id: String
                    get() = parent!!.id!!

                override val isDirectory: Boolean
                    get() = true

                override val name: String
                    get() = ""

                override val fullName: String
                    get() {

                        if (null == parent!!.path) {
                            return ""
                        }

                        val index = parent.path!!.indexOf(':')

                        return URLDecoder.decode(if (0 < index) parent.path.substring(index + 1) else parent.path, StandardCharsets.UTF_8) + '/'
                    }

                override val crc32: Long
                    get() = 0L

                override val size: Long
                    get() = 0L

                override val createdDateTime: Date
                    get() = Date()

                override val lastModifiedDateTime: Date
                    get() = Date()

                override val parent: OneDriveItem?
                    get() = null

                override val folder: FolderFacet
                    get() = FolderFacet()
            }
        }
    }
}
