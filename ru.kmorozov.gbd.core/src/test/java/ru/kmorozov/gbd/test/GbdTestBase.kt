package ru.kmorozov.gbd.test

import org.junit.Before
import org.mockito.Mockito
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.IGBDOptions
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.logger.output.ReceiverProvider

/**
 * Created by km on 22.01.2017.
 */
open class GbdTestBase {

    @Before
    fun init() {
        val options = Mockito.mock(IGBDOptions::class.java)
        Mockito.`when`(options.secureMode).thenReturn(false)

        ExecutionContext.initContext(true)

        GBDOptions.init(options)
    }
}
