package ru.kmorozov.gbd.core.logic.proxy

import org.apache.commons.net.telnet.TelnetClient
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.logger.Logger
import java.net.InetSocketAddress
import java.net.Proxy

class TorProxy : StableProxy {

    private val telnetClient = TelnetClient()
    private val readBytes = ByteArray(4096)
    private var lastResetedMillis = -1L

    constructor() : super(InetSocketAddress(TOR_HOST, TOR_HTTP_PORT), Proxy(Proxy.Type.SOCKS, InetSocketAddress(TOR_HOST, TOR_HTTP_PORT))) {
        if (GBDOptions.proxyListFile.equals("tor", ignoreCase = true)) {
            telnetClient.connect(TOR_HOST, TOR_CONTROL_PORT)

            telnetClient.getOutputStream().write("AUTHENTICATE \"tor_gbd_password\"\r\n".toByteArray())
            telnetClient.getOutputStream().flush()
            telnetClient.inputStream.read(readBytes)
            logger.info("Tor authentication result: " + String(readBytes))

            telnetClient.getOutputStream().write("SETEVENTS SIGNAL\r\n".toByteArray())
            telnetClient.getOutputStream().flush()
            telnetClient.inputStream.read(readBytes)
            logger.info("Tor SETEVENTS SIGNAL result: " + String(readBytes))
        }
    }

    override fun reset() {
        if (System.currentTimeMillis() - lastResetedMillis < RESET_MIN_DELAY)
            return

        synchronized(this) {
            super.reset()

            telnetClient.getOutputStream().write("SIGNAL NEWNYM\r\n".toByteArray())
            telnetClient.getOutputStream().flush()

            do {
                telnetClient.inputStream.read(readBytes)
                val strResult = String(readBytes)
            } while (!strResult.contains("650"))

            logger.info("Tor NEWNYM result: " + String(readBytes))
            lastResetedMillis = System.currentTimeMillis()

            Thread.sleep(RESET_MIN_DELAY)
        }
    }

    companion object {
        public val TOR_HOST = "localhost"
        public val TOR_HTTP_PORT = 9150
        val TOR_CONTROL_PORT = 9151

        private val RESET_MIN_DELAY = 10_000L

        private val logger = Logger.getLogger(TorProxy::class.java)
    }
}