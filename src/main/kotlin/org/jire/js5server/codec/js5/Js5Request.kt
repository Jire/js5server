package org.jire.js5server.codec.js5

import org.jire.js5server.codec.Incoming

sealed interface Js5Request : Incoming {

    sealed interface Group : Js5Request {
        val archive: Int
        val group: Int

        data class Prefetch(
            override val archive: Int,
            override val group: Int
        ) : Group

        data class OnDemand(
            override val archive: Int,
            override val group: Int
        ) : Group
    }

    object LoggedIn : Js5Request
    object LoggedOut : Js5Request

    data class Rekey(val key: Int) : Js5Request

    object Connected : Js5Request
    object Disconnected : Js5Request

}