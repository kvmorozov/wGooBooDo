package ru.kmorozov.library.data.model.dto

import com.google.common.base.Strings
import org.springframework.hateoas.RepresentationModel

/**
 * Created by sbt-morozov-kv on 02.02.2017.
 */
class UserDTO @JvmOverloads constructor(login: String = DEFAULT_USERNAME) : RepresentationModel<UserDTO>() {

    var login: String? = null

    init {
        this.login = if (Strings.isNullOrEmpty(login)) DEFAULT_USERNAME else login
    }

    companion object {

        const val DEFAULT_USERNAME = "anonymous"
    }
}
