package ru.kmorozov.library.data.model.dto;

import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.ResourceSupport;

/**
 * Created by sbt-morozov-kv on 02.02.2017.
 */
public class UserDTO extends ResourceSupport {

    public static final String DEFAULT_USERNAME = "anonymous";

    private String login;

    public UserDTO() {
        this(DEFAULT_USERNAME);
    }

    public UserDTO(String login) {
        this.login = StringUtils.isEmpty(login) ? DEFAULT_USERNAME : login;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
