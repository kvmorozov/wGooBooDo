package ru.kmorozov.gbd.core.logic.connectors.apache

import org.apache.hc.client5.http.cookie.BasicCookieStore
import org.apache.hc.client5.http.cookie.CookieStore
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.Proxy.UrlType

/**
 * Created by km on 22.05.2016.
 */
class GoogleApacheConnections : SimpleApacheConnections() {

    protected override fun getCookieStore(proxy: HttpHostExt): CookieStore {
        return cookieStoreMap.computeIfAbsent(proxy) {
            val cookieStore = BasicCookieStore()
            val cookies = proxy.getHeaders(UrlType.GOOGLE_BOOKS).cookie.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            for (cookieEntry in cookies) {
                val cookieParts = cookieEntry.split("=".toRegex(), 2).toTypedArray()

                if (1 < cookieParts.size) {
                    val cookie = BasicClientCookie(cookieParts[0], cookieParts[1])
                    cookie.domain = ".google.ru"
                    cookie.path = "/"
                    cookieStore.addCookie(cookie)
                }
            }
            cookieStore
        }
    }

    companion object {

        internal val INSTANCE = GoogleApacheConnections()
    }
}
