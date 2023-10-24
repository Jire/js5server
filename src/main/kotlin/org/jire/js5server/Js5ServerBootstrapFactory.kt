package org.jire.js5server

import io.netty.buffer.ByteBufAllocator
import io.netty.channel.*
import io.netty.channel.epoll.EpollChannelOption
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollMode
import io.netty.channel.socket.SocketChannel

class Js5ServerBootstrapFactory(
    private val channelInitializer: ChannelInitializer<SocketChannel>,

    private val allocator: ByteBufAllocator = ByteBufAllocator.DEFAULT,

    private val autoRead: Boolean = false,
    private val tcpNoDelay: Boolean = true,
    private val connectTimeoutMillis: Int = 30_000,

    private val ipTos: Int = 0b100_000_10,

    private val soSndBuf: Int = 2 shl 15,
    private val soRcvBuf: Int = 2 shl 15,

    private val writeBufferWatermarkLow: Int = 2 shl 18,
    private val writeBufferWatermarkHigh: Int = 2 shl 20
) : BootstrapFactory {

    override fun createServerBootstrap(
        parentGroup: EventLoopGroup,
        childGroup: EventLoopGroup,
        channel: Class<out ServerChannel>
    ) = super.createServerBootstrap(parentGroup, childGroup, channel).apply {
        option(ChannelOption.ALLOCATOR, allocator)
        childOption(ChannelOption.ALLOCATOR, allocator)

        childOption(ChannelOption.AUTO_READ, autoRead)
        childOption(ChannelOption.TCP_NODELAY, tcpNoDelay)
        childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)

        childOption(ChannelOption.IP_TOS, ipTos)

        childOption(ChannelOption.SO_SNDBUF, soSndBuf)
        childOption(ChannelOption.SO_RCVBUF, soRcvBuf)

        childOption(
            ChannelOption.WRITE_BUFFER_WATER_MARK,
            WriteBufferWaterMark(writeBufferWatermarkLow, writeBufferWatermarkHigh)
        )

        if (!autoRead && parentGroup is EpollEventLoopGroup) {
            // necessary to support disabling auto-read
            childOption(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED)
        }

        childHandler(channelInitializer)
    }

}