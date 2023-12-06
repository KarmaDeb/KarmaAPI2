package es.karmadev.api.netty.encoder;

import es.karmadev.api.channel.subscription.event.data.MessageReceiveEvent;
import es.karmadev.api.netty.ChannelNettyServer;
import es.karmadev.api.channel.data.BaseMessage;
import es.karmadev.api.netty.VirtualChannel;
import es.karmadev.api.netty.message.MessageBuilder;
import es.karmadev.api.netty.message.nat.Messages;
import io.netty.channel.*;

import java.util.concurrent.atomic.AtomicBoolean;

public class MessageHandler extends ChannelInboundHandlerAdapter {

    private final ChannelNettyServer server;

    public MessageHandler(final ChannelNettyServer server) {
        this.server = server;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        System.out.println("[SERVER]: " + msg);

        if (msg instanceof BaseMessage) {
            BaseMessage message = (BaseMessage) msg;
            long id = message.getId();

            Messages type = Messages.getById(id);
            if (type == null) {
                MessageReceiveEvent event = new MessageReceiveEvent(null, message.clone());
                server.propagate(event);
            } else {
                switch (type) {
                    case CHANNEL_JOIN: {
                        long joinChannelId = message.getInt64();
                        VirtualChannel channel = server.getChannels().stream().filter((ch) -> ch.getId() == joinChannelId).findAny().orElse(null);
                        if (channel == null || !channel.isPublished()) return;

                        Channel sender = ctx.channel();

                        MessageBuilder joinReply = new MessageBuilder();
                        AtomicBoolean add = new AtomicBoolean(false);
                        if (channel.getConnections().contains(sender)) {
                            joinReply.writeBoolean(false)
                                    .writeUTF("You are already connected!");
                        } else {
                            add.set(true);
                            joinReply.writeBoolean(true)
                                    .writeUTF(channel.getName());
                        }

                        BaseMessage joinEncoded = joinReply.build(Messages.CHANNEL_JOIN);
                        sender.writeAndFlush(joinEncoded).addListener((ChannelFutureListener) channelFuture -> {
                            if (channelFuture.isSuccess() && add.get()) {
                                channel.getConnections().add(sender);
                            }
                        });
                    }
                        break;
                    case CHANNEL_LEAVE: {
                        long leaveChannelId = message.getInt64();
                        VirtualChannel channel = server.getChannels().stream().filter((ch) -> ch.getId() == leaveChannelId).findAny().orElse(null);
                        if (channel == null || !channel.isPublished()) return;

                        Channel sender = ctx.channel();

                        MessageBuilder joinReply = new MessageBuilder();
                        if (channel.getConnections().contains(sender)) {
                            channel.getConnections().remove(sender);
                            joinReply.writeBoolean(true)
                                    .writeUTF(channel.getName());
                        } else {
                            joinReply.writeBoolean(false)
                                    .writeUTF("You are not connected!");
                        }

                        BaseMessage joinEncoded = joinReply.build(Messages.CHANNEL_LEAVE);
                        sender.writeAndFlush(joinEncoded);
                    }
                        break;
                    case DISCOVER: {
                        Channel sender = ctx.channel();

                        MessageBuilder discReply = new MessageBuilder();
                        server.getChannels().forEach((channel) -> discReply.writeInt64(
                                channel.getId()
                        ));

                        sender.writeAndFlush(discReply.build(Messages.DISCOVER));
                    }
                    break;
                }
            }

            return;
        }

        super.channelRead(ctx, msg);
    }
}
