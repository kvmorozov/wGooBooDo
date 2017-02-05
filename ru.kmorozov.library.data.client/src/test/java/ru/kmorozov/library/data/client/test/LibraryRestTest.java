package ru.kmorozov.library.data.client.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import ru.kmorozov.library.data.client.LibraryClient;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Created by sbt-morozov-kv on 02.02.2017.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LibraryClient.class)
@WebAppConfiguration
public class LibraryRestTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void login() throws Exception {
        mockMvc.perform(get("/login?login=user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login", is("user")))
                .andExpect(jsonPath("$._links").value(hasKey("root")))
        ;
    }
}
