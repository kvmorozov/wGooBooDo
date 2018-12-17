package ru.kmorozov.onedrive.client.walker

import ru.kmorozov.onedrive.client.OneDriveItem
import ru.kmorozov.onedrive.client.OneDriveProvider

import java.io.IOException
import java.util.Spliterators
import java.util.function.Predicate
import java.util.stream.Stream
import java.util.stream.StreamSupport

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */
object OneDriveWalkers {

    private const val MAX_MAX_DEPTH = 100
    private lateinit var itr: OneDriveIterator<OneDriveItem>

    @Throws(IOException::class)
    @JvmOverloads
    fun walk(api: OneDriveProvider, maxDepth: Int, skipCondition: (OneDriveItem) -> Boolean = { false }): Stream<OneDriveItem> {
        itr = OneDriveIterator(api, api.root, maxDepth, skipCondition)

        try {
            val stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(itr, 1), false)
            return stream.onClose { itr.close() }
        } catch (ex: RuntimeException) {
            itr.close()
            throw ex
        } catch (ex: Error) {
            itr.close()
            throw ex
        }

    }

    @Throws(IOException::class)
    fun walk(api: OneDriveProvider, skipCondition: (OneDriveItem) -> Boolean): Stream<OneDriveItem> {
        return walk(api, MAX_MAX_DEPTH, skipCondition)
    }

    fun stopAll() {
        itr.close()
    }
}
