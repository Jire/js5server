package org.jire.js5server.codec.init

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

class InitDecoder : ByteToMessageDecoder() {

    private enum class State {
        OPCODE,
        PAYLOAD
    }

    private var state = State.OPCODE

    private var opcode = -1
    private var length = -1

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (state == State.OPCODE) {
            opcode = buf.readUnsignedByte().toInt()
            when (opcode) {
                14 -> out += InitRequest.Rs2
                15 -> {
                    length = 4
                    state = State.PAYLOAD
                }

                else -> ctx.close()
            }
        }

        if (state == State.PAYLOAD) {
            if (!buf.isReadable(length)) return

            when (opcode) {
                15 -> {
                    val build = buf.readInt()
                    out += InitRequest.Js5(build)

                    state = State.OPCODE
                }

                else -> ctx.close()
            }
        }
    }

}