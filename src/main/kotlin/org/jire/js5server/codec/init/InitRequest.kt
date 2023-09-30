package org.jire.js5server.codec.init

import org.jire.js5server.codec.Incoming

sealed interface InitRequest : Incoming {

    data object Rs2 : InitRequest

    data class Js5(
        val version: Int
    ) : InitRequest

}