package ru.kmorozov.library.data.model.dto

import org.springframework.hateoas.RepresentationModel
import org.springframework.util.StringUtils

/**
 * Created by sbt-morozov-kv on 02.02.2017.
 */
class UserDTO @JvmOverloads constructor(login: String = DEFAULT_USERNAME) : RepresentationModel<UserDTO>() {

    var login: String? = null

    init {
        this.login = if (StringUtils.isEmpty(login)) DEFAULT_USERNAME else login
    }

    companion object {

        const val DEFAULT_USERNAME = "anonymous"
    }
}
