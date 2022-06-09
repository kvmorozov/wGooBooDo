package ru.kmorozov.library.data.server.options

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import ru.kmorozov.gbd.core.config.IStorage
import ru.kmorozov.library.data.server.storage.ServerStorage
import ru.kmorozov.onedrive.client.OneDriveItem
import ru.kmorozov.onedrive.client.OneDriveProvider

@Component
@Qualifier("remote")
class ServerGBDOptions : AbstractServerGBDOptions() {

    @Autowired
    @Lazy
    private lateinit var api: OneDriveProvider

    @Autowired
    @Lazy
    private val root: OneDriveItem? = null

    override val storage: IStorage
        get() = ServerStorage(api, root)

    override fun serverMode(): Boolean {
        return true
    }

    override val debugEnabled: Boolean
        get() = false
}
