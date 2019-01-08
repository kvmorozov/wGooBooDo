package ru.kmorozov.gbd.logger.output

import ru.kmorozov.gbd.logger.consumers.AbstractOutputReceiver

object ReceiverProvider {

    fun getReceiver(debug: Boolean = false): AbstractOutputReceiver {
        return if (debug) DebugReceiver.INSTANCE else DefaultReceiver.INSTANCE
    }
}