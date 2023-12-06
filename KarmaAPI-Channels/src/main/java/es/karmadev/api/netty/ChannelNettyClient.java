package es.karmadev.api.netty;

import es.karmadev.api.channel.IClient;
import es.karmadev.api.channel.com.IConnection;
import es.karmadev.api.netty.com.LocalConnection;
import es.karmadev.api.netty.encoder.DataDecoder;
import es.karmadev.api.netty.encoder.DataEncoder;
import es.karmadev.api.netty.encoder.client.ClientHandler;
import es.karmadev.api.object.var.Variable;
import es.karmadev.api.schedule.task.completable.BiTaskCompletor;
import es.karmadev.api.schedule.task.completable.late.BiLateTask;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.SocketAddress;
import java.util.concurrent.ThreadLocalRandom;

public class ChannelNettyClient implements IClient {

    private final EventLoopGroup workGroup = new NioEventLoopGroup();
    private final long id = ThreadLocalRandom.current().nextLong();

    private boolean bridgeSupport = false;
    private Channel channel;
    private IConnection connection;

    /**
     * Get the client ID
     *
     * @return the client ID
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     * Connect the client to a server
     *
     * @param address the server address
     * @param bridge  if the connection supports
     *                bridging
     * @return the connection task
     */
    @Override
    public BiTaskCompletor<Boolean, IConnection> connect(final SocketAddress address, final boolean bridge) {
        BiTaskCompletor<Boolean, IConnection> task = new BiLateTask<>();
        this.bridgeSupport = bridge;

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

        Variable<IConnection> connectionVariable = Variable.notNull(null);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                DataEncoder encoder = new DataEncoder(connectionVariable);
                DataDecoder decoder = new DataDecoder();
                ClientHandler handler = new ClientHandler();

                ch.pipeline().addLast(
                        encoder,
                        decoder,
                        handler
                );
            }
        });

        bootstrap.connect(address).addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                this.channel = channelFuture.channel();
                connection = new LocalConnection(this, channel);

                connectionVariable.getReference()
                        .set(connection);

                task.complete(true, connection);
            } else {
                task.complete(false, null, channelFuture.cause());
            }
        });

        return task;
    }

    /**
     * Get if the client is connected
     *
     * @return the client connection
     * status
     */
    @Override
    public boolean isConnected() {
        return channel != null && channel.isOpen();
    }

    /**
     * Get the client connection
     *
     * @return the client connection
     */
    @Override
    public IConnection getConnection() {
        return connection;
    }

    /**
     * Get if the connection supports bridging.
     * Bridging allows two connections to be
     * directly connected through a virtual channel
     *
     * @return if the connection supports bridging
     */
    @Override
    public boolean supportsBridging() {
        return bridgeSupport;
    }
}
