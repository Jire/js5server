package org.jire.js5server

import io.netty.channel.ChannelFuture
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Js5Service(
    config: Js5ServiceConfig,
    groupRepository: Js5GroupRepository,

    bootstrapFactory: BootstrapFactory = Js5ServerBootstrapFactory(
        Js5ChannelInitializer(config, groupRepository)
    )
) : AutoCloseable {

    private val parentGroup = bootstrapFactory.createEventLoopGroup(1)
    private val childGroup = bootstrapFactory.createEventLoopGroup()

    private val bootstrap = bootstrapFactory.createServerBootstrap(parentGroup, childGroup)

    fun bind(port: Int): ChannelFuture {
        logger.debug("Binding to port {} with group type \"{}\"", port, parentGroup::class.java)
        return bootstrap.bind(port)
    }

    fun listen(port: Int) = bind(port).addListener {
        logger.info("Listening on port {}", port)
    }

    override fun close() {
        childGroup.shutdownGracefully()
        parentGroup.shutdownGracefully()
    }

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(Js5Service::class.java)
    }

}