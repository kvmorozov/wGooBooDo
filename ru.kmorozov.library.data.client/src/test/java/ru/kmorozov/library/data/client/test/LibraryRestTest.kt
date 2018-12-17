package ru.kmorozov.library.data.client.test

import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import ru.kmorozov.library.data.client.LibraryClient

/**
 * Created by sbt-morozov-kv on 02.02.2017.
 */

@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(LibraryClient::class))
@WebAppConfiguration
class LibraryRestTest {

    private var mockMvc: MockMvc? = null

    @Autowired
    private val webApplicationContext: WebApplicationContext? = null

    @Before
    fun setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext!!).build()
    }

    @Test
    @Throws(Exception::class)
    fun login() {
        mockMvc!!.perform(MockMvcRequestBuilders.get("/login?login=user"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.login", Matchers.`is`("user")))
                .andExpect(MockMvcResultMatchers.jsonPath("$._links").value(Matchers.hasKey("root")))
    }
}
