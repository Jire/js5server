package org.jire.js5server

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        val service = Js5Service()

        val ports = args.map { it.toInt() }.ifEmpty { listOf(443, 43594, 50000) }
        for (port in ports) service.listen(port)
    }

}