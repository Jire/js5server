package org.jire.js5server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.EventLoopGroup
import io.netty.channel.ServerChannel
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.incubator.channel.uring.IOUring
import io.netty.incubator.channel.uring.IOUringEventLoopGroup
import io.netty.incubator.channel.uring.IOUringServerSocketChannel

interface BootstrapFactory {

    fun createEventLoopGroup(threads: Int = 0): EventLoopGroup =
        when {
            IOUring.isAvailable() -> IOUringEventLoopGroup(threads)
            Epoll.isAvailable() -> EpollEventLoopGroup(threads)
            KQueue.isAvailable() -> KQueueEventLoopGroup(threads)
            else -> NioEventLoopGroup(threads)
        }

    fun getServerSocketChannelClass(): Class<out ServerChannel> =
        when {
            IOUring.isAvailable() -> IOUringServerSocketChannel::class.java
            Epoll.isAvailable() -> EpollServerSocketChannel::class.java
            KQueue.isAvailable() -> KQueueServerSocketChannel::class.java
            else -> NioServerSocketChannel::class.java
        }

    fun createServerBootstrap(
        parentGroup: EventLoopGroup = createEventLoopGroup(1),
        childGroup: EventLoopGroup = createEventLoopGroup(),
        channel: Class<out ServerChannel> = getServerSocketChannelClass()
    ) = ServerBootstrap().apply {
        group(parentGroup, childGroup)
        channel(channel)
    }

}