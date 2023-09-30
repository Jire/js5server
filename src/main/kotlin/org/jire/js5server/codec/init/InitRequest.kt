package org.jire.js5server.codec.init

import org.jire.js5server.codec.Request

sealed interface InitRequest : Request {

    data object Rs2 : InitRequest

    data class Js5(
        val version: Int
    ) : InitRequest

}