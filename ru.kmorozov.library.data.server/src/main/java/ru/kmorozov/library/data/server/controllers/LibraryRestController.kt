package ru.kmorozov.library.data.server.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Lazy
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.library.data.loader.processors.gbd.GbdLocalProcessor
import ru.kmorozov.library.data.server.condition.LibraryEnabledCondition

/**
 * Created by km on 19.12.2016.
 */

@RestController
@ComponentScan(basePackageClasses = arrayOf(GbdLocalProcessor::class))
@Conditional(LibraryEnabledCondition::class)
class LibraryRestController {

    @Autowired
    @Lazy
    private lateinit var gbdProcessor: GbdLocalProcessor

    @RequestMapping("/gbdUpdate")
    fun gbdUpdate() {
        gbdProcessor.process()
    }

    @RequestMapping("/gbdLoadLocal")
    fun gbdLoad(@RequestParam(name = "bookId", required = false) bookId: String) {
        gbdProcessor.load(bookId)
    }

    companion object {

        protected val logger = Logger.getLogger(LibraryRestController::class.java)
    }
}
