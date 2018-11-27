package ru.kmorozov.library.data.model.dto;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.util.StringUtils;

/**
 * Created by sbt-morozov-kv on 02.02.2017.
 */
public class UserDTO extends ResourceSupport {

    public static final String DEFAULT_USERNAME = "anonymous";

    private String login;

    public UserDTO() {
        this(DEFAULT_USERNAME);
    }

    public UserDTO(final String login) {
        this.login = StringUtils.isEmpty(login) ? DEFAULT_USERNAME : login;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(final String login) {
        this.login = login;
    }
}
