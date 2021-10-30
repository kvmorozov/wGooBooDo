package ru.kmorozov.library.data.loader.processors.gbd

import com.google.common.base.Strings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.context.IBookListProducer
import ru.kmorozov.gbd.core.logic.library.LibraryFactory
import java.util.*

@Component
open class ServerProducer : IBookListProducer {

    private var ids: Set<String>? = null

    @Value("\${library.gbd.ids}")
    var defaultIds: String? = null

    @Autowired
    @Lazy
    private val dbCtx: DbContextLoader? = null

    override val bookIds: Set<String>
        get() {
            val bookId = GBDOptions.bookId

            if (!Strings.isNullOrEmpty(bookId) && LibraryFactory.isValidId(bookId))
                ids = HashSet(listOf(bookId))

            if (ids == null)
                if (Strings.isNullOrEmpty(defaultIds))
                    ids = dbCtx!!.bookIdsList
                else
                    ids = HashSet(Arrays.asList(*defaultIds!!.split(",".toRegex()).dropLastWhile { it.isEmpty }.toTypedArray()))

            return ids!!
        }
}
