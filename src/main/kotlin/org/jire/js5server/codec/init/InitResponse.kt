package org.jire.js5server.codec.init

import org.jire.js5server.codec.ClientResponse
import org.jire.js5server.codec.Response

sealed interface InitResponse : Response {

    val response: ClientResponse

    data class Rs2(
        override val response: ClientResponse,
        val sessionKey: Long
    ) : InitResponse

    data class Js5(
        override val response: ClientResponse
    ) : InitResponse

}