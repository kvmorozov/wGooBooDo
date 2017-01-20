package ru.kmorozov.library.data.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by sbt-morozov-kv on 17.01.2017.
 */

@Controller
public class LibraryClietnController {

    @RequestMapping(value = "/")
    public String index() {
        return "index";
    }
}
