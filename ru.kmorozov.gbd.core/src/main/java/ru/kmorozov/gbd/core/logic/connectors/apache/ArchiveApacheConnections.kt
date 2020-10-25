package ru.kmorozov.gbd.core.logic.connectors.apache

import org.apache.http.client.CookieStore
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.cookie.Cookie
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.http.message.BasicHeader
import org.apache.http.message.BasicNameValuePair
import ru.kmorozov.db.utils.Mapper
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.logger.Logger
import kotlin.system.exitProcess

class ArchiveApacheConnections : SimpleApacheConnections() {

    protected override fun getCookieStore(proxy: HttpHostExt): CookieStore {
        val cookieStore = BasicCookieStore()

        try {
            val client = SimpleApacheConnections.INSTANCE.getClient()
            val loginRq = HttpPost("https://archive.org/account/login.php")

            val loginParams: MutableList<BasicNameValuePair> = ArrayList<BasicNameValuePair>()
            loginParams.add(BasicNameValuePair("username", GBDOptions.authOptions!!.login))
            loginParams.add(BasicNameValuePair("password", GBDOptions.authOptions!!.password))
            loginParams.add(BasicNameValuePair("remember", "CHECKED"))
            loginParams.add(BasicNameValuePair("action", "login"))
            loginParams.add(BasicNameValuePair("submit", "Log in"))

            loginRq.setEntity(UrlEncodedFormEntity(loginParams))

            loginRq.addHeader(BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"))
            loginRq.addHeader(BasicHeader("Accept-Encoding", "gzip, deflate, br"))
            loginRq.addHeader(BasicHeader("Accept-Language", "u-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7,de;q=0.6,fr;q=0.5,es;q=0.4"))
            loginRq.addHeader(BasicHeader("Cache-Control", "max-age=0"))
            loginRq.addHeader(BasicHeader("Connection", "keep-alive"))
            loginRq.addHeader(BasicHeader("Content-Type", "application/x-www-form-urlencoded"))
            loginRq.addHeader(BasicHeader("Cookie", "test-cookie=1"))
            loginRq.addHeader(BasicHeader("Host", "archive.org"))
            loginRq.addHeader(BasicHeader("Origin", "https://archive.org"))
            loginRq.addHeader(BasicHeader("Referer", "https://archive.org/account/login.php"))
            loginRq.addHeader(BasicHeader("Upgrade-Insecure-Requests", "1"))

            val loginRs = client.execute(loginRq)

            loginRs.getHeaders("Set-Cookie").forEach {
                val cookieParts = it.value.split(";")[0].split("=".toRegex(), 2).toTypedArray()
                cookieStore.addCookie(createCookie(cookieParts[0], cookieParts[1]))
            }

            val loanRq = HttpPost("https://archive.org/services/loans/loan/")
            val loanParams: MutableList<BasicNameValuePair> = ArrayList<BasicNameValuePair>()
            loanParams.add(BasicNameValuePair("action", "create_token"))
            loanParams.add(BasicNameValuePair("identifier", GBDOptions.bookId))
            loanRq.setEntity(UrlEncodedFormEntity(loanParams))

            val loanRs = builder.setDefaultCookieStore(cookieStore).setDefaultHeaders(loginRq.allHeaders.asList()).build().execute(loanRq)
            val loanJson: Map<String, String> = Mapper.gson.fromJson(String(loanRs.entity.content.readAllBytes()), Mapper.mapType)

            val success = loanJson.containsKey("success") && loanJson["success"]!!.toBoolean()

            if (!success) {
                logger.error("You have to loan ${GBDOptions.bookId} first!")
                exitProcess(-1)
            }

            val token = loanJson["token"]!!

            cookieStore.addCookie(createCookie("loan-" + GBDOptions.bookId, token))

            loanRs.getHeaders("Set-Cookie").map { it.value }.forEach {
                val cookieParts = it.split(";")[0].split("=".toRegex(), 2).toTypedArray()
                cookieStore.addCookie(createCookie(cookieParts[0], cookieParts[1]))
            }
        } catch (ex: Exception) {
            logger.error("Unknown error when initializing ${GBDOptions.bookId}!")
            ex.printStackTrace()

            exitProcess(-1)
        }

        return cookieStore
    }

    fun createCookie(name: String, value: String): Cookie {
        val cookie = BasicClientCookie(name, value)
        cookie.domain = ".archive.org"
        cookie.path = "/"

        return cookie
    }

    companion object {

        private val logger = Logger.getLogger(ArchiveApacheConnections::class.java)
        internal val INSTANCE = ArchiveApacheConnections()
    }
}