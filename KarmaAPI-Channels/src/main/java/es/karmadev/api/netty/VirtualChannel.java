package es.karmadev.api.netty;

import es.karmadev.api.channel.IChannel;
import es.karmadev.api.channel.IServer;
import es.karmadev.api.channel.com.IBridge;
import es.karmadev.api.channel.subscription.AChannelSubscription;
import es.karmadev.api.channel.subscription.event.NetworkEvent;
import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.channel.data.BaseMessage;
import es.karmadev.api.netty.message.MessageBuilder;
import es.karmadev.api.netty.message.nat.Messages;
import es.karmadev.api.schedule.task.completable.TaskCompletor;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a virtual channel
 * through a netty server
 */
public class VirtualChannel implements IChannel {

    private final static SecureRandom random = new SecureRandom();

    private final long id = random.nextLong();
    private final List<AChannelSubscription> subscriptions = new ArrayList<>();
    @Getter
    private final List<Channel> connections = new ArrayList<>();

    private boolean published = false;

    private final IServer server;
    private String name;

    public VirtualChannel(final IServer server, final String name) {
        this.server = server;
        this.name = name;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean publish() {
        if (published) return false;

        MessageBuilder builder = new MessageBuilder();
        builder.writeUTF(name);
        try {
            server.write(builder.build(Messages.CHANNEL_OPEN));
            published = true;

            return true;
        } catch (IOException ex) {
            ExceptionCollector.catchException(VirtualChannel.class, ex);
            return false;
        }
    }

    /**
     * Get if the channel has been
     * published
     *
     * @return if the channel has been
     * published
     */
    @Override
    public boolean isPublished() {
        return published;
    }

    @Override
    public TaskCompletor<IBridge> createBridge(final long to) {
        return null;
    }

    @Override
    public void write(final BaseMessage message) {
        connections.forEach((sk) -> sk.writeAndFlush(message));
    }

    @Override
    public void subscribe(final AChannelSubscription subscription) {
        if (subscriptions.contains(subscription)) return;
        subscriptions.add(subscription);
    }

    @Override
    public void unsubscribe(final AChannelSubscription subscription) {
        subscriptions.remove(subscription);
    }

    @Override
    public void handle(final NetworkEvent event) {

    }
}
