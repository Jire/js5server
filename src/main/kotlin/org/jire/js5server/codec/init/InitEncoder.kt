package org.jire.js5server.codec.init

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

class InitEncoder : MessageToByteEncoder<InitResponse>() {

    override fun encode(ctx: ChannelHandlerContext, packet: InitResponse, out: ByteBuf) {
        when (packet) {
            is InitResponse.Rs2 -> ctx.close()

            is InitResponse.Js5 -> {
                out.writeByte(packet.response.id)
            }
        }
    }

}