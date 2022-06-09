package ru.kmorozov.library.data.server.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Lazy
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.library.data.loader.processors.IGbdProcessor
import ru.kmorozov.library.data.server.condition.LibraryEnabledCondition
import ru.kmorozov.library.data.server.options.ServerGBDOptions
import javax.annotation.PostConstruct

/**
 * Created by km on 19.12.2016.
 */

@RestController
@ComponentScan(basePackageClasses = [GbdConfiguration::class, ServerGBDOptions::class])
@Conditional(LibraryEnabledCondition::class)
open class LibraryRestController {

    @Autowired
    @Lazy
    @Qualifier("local")
    private lateinit var local: IGbdProcessor

    @Autowired
    @Qualifier("remote")
    private lateinit var options: ServerGBDOptions

    @PostMapping("/gbdUpdate")
    fun gbdUpdate() {
        local.process()
    }

    @PostMapping("/gbdLoadLocal")
    fun gbdLoad(@RequestParam(name = "bookId", required = true) bookId: String) {
        local.addBook(bookId)
    }

    @PostConstruct
    fun init() {
        GBDOptions.init(options)
        logger = Logger.getLogger(GBDOptions.debugEnabled, LibraryRestController::class.java)
    }

    companion object {

        protected lateinit var logger: Logger
    }
}
