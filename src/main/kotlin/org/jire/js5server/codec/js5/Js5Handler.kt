package org.jire.js5server.codec.js5

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.group.ChannelGroup
import io.netty.channel.group.DefaultChannelGroup
import io.netty.handler.codec.DecoderException
import io.netty.handler.timeout.IdleStateEvent
import io.netty.util.concurrent.GlobalEventExecutor
import org.jctools.queues.MessagePassingQueue
import org.jctools.queues.SpscArrayQueue
import org.jire.js5server.Js5GroupRepository
import org.jire.js5server.PipelineConstants
import org.jire.js5server.PipelineConstants.HANDLER
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class Js5Handler(
    private val groupRepository: Js5GroupRepository
) : SimpleChannelInboundHandler<Js5Request>() {

    private lateinit var prefetchQueue: MessagePassingQueue<ByteBuf>
    private lateinit var onDemandQueue: MessagePassingQueue<ByteBuf>

    private var loggedIn = false

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        prefetchQueue = SpscArrayQueue(QUEUE_CAPACITY)
        onDemandQueue = SpscArrayQueue(QUEUE_CAPACITY)

        val channel = ctx.channel()
        channels.add(channel)
        channel.config().isAutoRead = true
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: Js5Request) {
        when (msg) {
            is Js5Request.Group.Prefetch -> {
                val response = groupRepository[msg.archive, msg.group]
                    ?: throw DecoderException("Invalid prefetch group request ($msg)")

                if (!prefetchQueue.offer(response))
                    throw IllegalStateException("Filled prefetch queue ($msg)")
            }

            is Js5Request.Group.OnDemand -> {
                val response = groupRepository[msg.archive, msg.group]
                    ?: throw DecoderException("Invalid on-demand group request ($msg)")

                if (!onDemandQueue.offer(response))
                    throw IllegalStateException("Filled on-demand queue ($msg)")
            }

            Js5Request.LoggedIn -> {
                loggedIn = true
            }

            Js5Request.LoggedOut -> {
                loggedIn = false
            }

            else -> {}
        }
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        poll(ctx)

        val channel = ctx.channel()
        if (!channel.isWritable) {
            channel.config().isAutoRead = false
        }
    }

    override fun channelWritabilityChanged(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        if (channel.isWritable) {
            channel.config().isAutoRead = true
        }
    }

    private fun poll(ctx: ChannelHandlerContext): Boolean {
        var written = false

        for (i in 1..QUEUE_CAPACITY) {
            val request = onDemandQueue.poll() ?: break

            ctx.write(request.retainedDuplicate(), ctx.voidPromise())
            written = true
        }

        if (written) {
            ctx.flush()
            return true
        }
        return false
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is IdleStateEvent) {
            ctx.close()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.error("Exception in JS5", cause)
    }

    companion object {

        private const val QUEUE_CAPACITY = 200

        private const val PREFETCH_QUEUE_PERIOD_MILLIS = 200L

        private val logger: Logger = LoggerFactory.getLogger(Js5Handler::class.java)

        private val channels: ChannelGroup = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)

        @JvmStatic
        fun startPrefetching(): ScheduledFuture<*> =
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
                for (channel in channels) {
                    if (!channel.isWritable) continue

                    val handler = channel.pipeline().get(HANDLER)
                    if (handler is Js5Handler) {
                        val request = handler.prefetchQueue.poll() ?: continue
                        channel.writeAndFlush(request.retainedDuplicate(), channel.voidPromise())
                    }
                }
            }, PREFETCH_QUEUE_PERIOD_MILLIS, PREFETCH_QUEUE_PERIOD_MILLIS, TimeUnit.MILLISECONDS)

    }

}