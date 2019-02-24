package ru.kmorozov.library.data.server.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import ru.kmorozov.library.data.loader.processors.IGbdProcessor
import ru.kmorozov.library.data.loader.processors.gbd.GbdLocalProcessor
import ru.kmorozov.library.data.loader.processors.gbd.GbdRemoteProcessor

@Configuration
@ComponentScan(basePackageClasses = arrayOf(GbdLocalProcessor::class, GbdRemoteProcessor::class))
open class GbdConfiguration {

    @Autowired
    @Lazy
    private lateinit var gbdLocalProcessor: GbdLocalProcessor

    @Autowired
    @Lazy
    private lateinit var gbdRemoteProcessor: GbdRemoteProcessor

    fun gbdProcessor(): IGbdProcessor {
        return if (isLocal()) gbdLocalProcessor else gbdRemoteProcessor
    }

    fun isLocal(): Boolean {
        return System.getProperty("os.name").contains("Windows")
    }
}