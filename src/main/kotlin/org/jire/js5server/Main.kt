package org.jire.js5server

import org.jire.js5server.codec.js5.Js5Handler
import java.io.FileInputStream
import java.util.*

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        val props = Properties().apply {
            FileInputStream("js5server.properties").use {
                load(it)
            }
        }
        val config = Js5ServiceConfig(props)

        val service = Js5Service(config)

        for (port in config.listenPorts) {
            service.listen(port)
        }

        if (config.supportPrefetch) {
            Js5Handler.startPrefetching()
        }
    }

}