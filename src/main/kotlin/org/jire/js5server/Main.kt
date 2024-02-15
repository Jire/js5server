package org.jire.js5server

import org.jire.js5server.codec.js5.Js5Handler
import org.openrs2.cache.DiskStore
import java.io.FileInputStream
import java.nio.file.Path
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

        val groupRepository = Openrs2Js5GroupRepository(
            store = DiskStore.open(Path.of(config.cachePath))
        ).apply(Js5GroupRepository::load)

        val service = Js5Service(config, groupRepository)

        for (port in config.listenPorts) {
            service.listen(port)
        }

        if (config.supportPrefetch) {
            Js5Handler.startPrefetching()
        }
    }

}