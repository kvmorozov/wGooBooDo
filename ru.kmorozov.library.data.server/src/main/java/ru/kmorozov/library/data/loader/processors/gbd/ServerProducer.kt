package ru.kmorozov.library.data.loader.processors.gbd

import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.context.IBookListProducer
import ru.kmorozov.gbd.core.logic.library.LibraryFactory

import java.util.Arrays
import java.util.Collections
import java.util.HashSet

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

            if (!StringUtils.isEmpty(bookId) && LibraryFactory.isValidId(bookId))
                ids = HashSet(listOf(bookId))

            if (ids == null)
                if (StringUtils.isEmpty(defaultIds))
                    ids = dbCtx!!.bookIdsList
                else
                    ids = HashSet(Arrays.asList(*defaultIds!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))

            return ids!!
        }
}
