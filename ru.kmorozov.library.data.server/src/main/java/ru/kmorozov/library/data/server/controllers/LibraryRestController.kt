package ru.kmorozov.library.data.server.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Lazy
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.library.data.loader.processors.IGbdProcessor
import ru.kmorozov.library.data.server.condition.LibraryEnabledCondition

/**
 * Created by km on 19.12.2016.
 */

@RestController
@ComponentScan(basePackageClasses = arrayOf(GbdConfiguration::class))
@Conditional(LibraryEnabledCondition::class)
class LibraryRestController {

    @Autowired
    @Lazy
    @Qualifier("local")
    private lateinit var gbdProcessor: IGbdProcessor

    @PostMapping("/gbdUpdate")
    fun gbdUpdate() {
        gbdProcessor.process()
    }

    @PostMapping("/gbdLoadLocal")
    fun gbdLoad(@RequestParam(name = "bookId", required = true) bookId: String) {
        gbdProcessor.load(bookId)
    }

    companion object {

        protected val logger = Logger.getLogger(LibraryRestController::class.java)
    }
}
