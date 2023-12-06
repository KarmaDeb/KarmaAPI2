package es.karmadev.api.netty;

import es.karmadev.api.channel.IServer;
import es.karmadev.api.channel.com.IConnection;
import es.karmadev.api.channel.subscription.AChannelSubscription;
import es.karmadev.api.channel.subscription.event.NetworkEvent;
import es.karmadev.api.netty.encoder.DataDecoder;
import es.karmadev.api.netty.encoder.DataEncoder;
import es.karmadev.api.channel.data.BaseMessage;
import es.karmadev.api.netty.encoder.MessageHandler;
import es.karmadev.api.netty.message.MessageBuilder;
import es.karmadev.api.object.var.Variable;
import es.karmadev.api.schedule.task.completable.TaskCompletor;
import es.karmadev.api.schedule.task.completable.late.LateTask;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ChannelNettyServer implements IServer {

    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final List<AChannelSubscription> subscriptions = new ArrayList<>();
    private final List<VirtualChannel> channels = new ArrayList<>();

    private final SocketAddress address;
    private final long id = ThreadLocalRandom.current().nextLong();

    private ServerChannel server;

    public ChannelNettyServer() throws SocketException {
        this.address = findAddress(4653);
    }

    public ChannelNettyServer(final String host) {
        this(InetSocketAddress.createUnresolved(host, 4653));
    }

    public ChannelNettyServer(final int port) throws SocketException {
        this.address = findAddress(port);
    }

    public ChannelNettyServer(final String host, final int port) {
        this(InetSocketAddress.createUnresolved(host, port));
    }

    public ChannelNettyServer(final SocketAddress address) {
        this.address = address;
    }


    private static InetSocketAddress findAddress(final int port) throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        InetSocketAddress socket = null;
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isLoopback() || networkInterface.isVirtual()) continue;

            List<InterfaceAddress> addresses = networkInterface.getInterfaceAddresses();
            if (addresses.isEmpty()) continue;

            for (InterfaceAddress address : addresses) {
                if (address == null) continue;

                InetAddress mainAddress = address.getAddress();
                InetAddress broadcast = address.getBroadcast();

                if (mainAddress == null || broadcast == null) continue;
                if (mainAddress.equals(broadcast)) continue;

                String host = mainAddress.getHostAddress();
                if (host == null) continue;

                socket = new InetSocketAddress(host, port);
                break;
            }
        }

        return socket;
    }

    /**
     * Get the server ID
     *
     * @return the server ID
     */
    @Override
    public long getId() {
        return id;
    }

    @Override
    public SocketAddress getAddress() {
        return address;
    }

    @Override
    public Collection<VirtualChannel> getChannels() {
        return Collections.unmodifiableList(channels);
    }

    @Override
    public VirtualChannel createChannel(final String name) {
        VirtualChannel channel = new VirtualChannel(this, name);
        channels.add(channel);

        return channel;
    }

    @Override
    public void addSubscriptor(final AChannelSubscription subscription) {
        if (subscriptions.contains(subscription)) return;
        subscriptions.add(subscription);
    }

    @Override
    public void removeSubscriptor(final AChannelSubscription subscription) {
        subscriptions.remove(subscription);
    }

    @Override
    public void propagate(final NetworkEvent event) {
        channels.forEach((channel -> channel.handle(event)));
    }

    @Override
    public void write(final BaseMessage message) {
        MessageBuilder idContainer = new MessageBuilder();
        idContainer.writeInt64(id);

        try {
            BaseMessage idHolder = idContainer.build(message.getId());
            BaseMessage merged = MessageBuilder.insertBefore(message, idHolder);
            server.writeAndFlush(merged);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean isRunning() {
        return server != null && server.isOpen();
    }

    @Override
    public TaskCompletor<Boolean> start() {
        LateTask<Boolean> task = new LateTask<>();
        if (server != null && server.isOpen()) {
            task.complete(true);
            return task;
        }

        Variable<IConnection> connection = Variable.notNull(null);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        DataEncoder encoder = new DataEncoder(connection);
                        DataDecoder decoder = new DataDecoder();
                        MessageHandler handler = new MessageHandler(ChannelNettyServer.this);

                        ch.pipeline().addLast(encoder, decoder, handler);
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.SO_KEEPALIVE, true);

        bootstrap.bind(address).addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                this.server = (ServerChannel) channelFuture.channel();
                task.complete(this.server.isOpen());
            } else {
                Throwable error = channelFuture.cause();
                task.complete(false, error);
            }
        });

        return task;
    }

    public void stop() {
        if (server == null || !server.isOpen()) return;

        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        server.close();
    }
}
