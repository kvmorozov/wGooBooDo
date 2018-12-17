package ru.kmorozov.onedrive.client.authoriser

import java.io.IOException
import java.nio.file.Path

interface AuthorisationProvider {

    val accessToken: String

    @Throws(IOException::class)
    fun refresh()

    object FACTORY {
        @Throws(IOException::class)
        fun create(keyFile: Path, clientId: String, clientSecret: String): AuthorisationProvider {
            return OneDriveAuthorisationProvider(keyFile, clientId, clientSecret)
        }

        fun printAuthInstructions(clientId: String) {
            OneDriveAuthorisationProvider.printAuthInstructions(clientId)
        }
    }
}
