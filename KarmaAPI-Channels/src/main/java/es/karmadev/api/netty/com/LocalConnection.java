package es.karmadev.api.netty.com;

import es.karmadev.api.channel.com.IConnection;
import es.karmadev.api.channel.data.BaseMessage;
import es.karmadev.api.channel.exception.NetException;
import es.karmadev.api.channel.exception.connection.CloseException;
import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.netty.ChannelNettyClient;
import es.karmadev.api.netty.com.secure.SecureGen;
import es.karmadev.api.netty.message.MessageBuilder;
import es.karmadev.api.netty.message.nat.Messages;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import javax.crypto.*;
import java.io.IOException;
import java.security.*;

/**
 * Represents a local connection
 */
public class LocalConnection implements IConnection {

    private final ChannelNettyClient client;
    private final Channel channel;

    private final KeyPair pair;
    private final SecretKey secret;
    private final byte[] encodedSecret;

    public LocalConnection(final ChannelNettyClient client, final Channel channel) throws IOException {
        this.client = client;
        this.channel = channel;

        pair = SecureGen.generateKeyPair();
        secret = SecureGen.generateSecret();
        byte[] encoded = null;
        if (pair != null && secret != null) {
            encoded = SecureGen.protectKey(secret, pair.getPublic());
        }
        this.encodedSecret = encoded;

        if (this.encodedSecret != null) {
            MessageBuilder builder = new MessageBuilder();
            builder.write(encodedSecret);

            BaseMessage message = builder.build(Messages.KEY_EXCHANGE);
            channel.writeAndFlush(message);
        }
    }

    /**
     * Write a message on the connection
     *
     * @param message the message to write
     */
    @Override
    public void write(final BaseMessage message) {
        MessageBuilder idContainer = new MessageBuilder();
        idContainer.writeInt64(client.getId());

        try {
            BaseMessage idHolder = idContainer.build(message.getId());
            BaseMessage merged = MessageBuilder.insertBefore(message, idHolder);
            channel.writeAndFlush(merged);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get if the connection is encrypted.
     *
     * @return if the connection uses
     * encrypted data
     */
    @Override
    public boolean isEncrypted() {
        return pair != null && secret != null &&
                encodedSecret != null;
    }

    /**
     * Tries to close the connection. Once this
     * method is called, the connection will get
     * closed even though {@link NetException exception} is
     * thrown.
     *
     * @throws NetException if there's a network
     *                      problem while closing the connection
     */
    @Override
    public void close() throws NetException {
        try {
            ChannelFuture future = channel.close().sync();
            if (!future.isSuccess()) {
                Throwable cause = future.cause();
                if (cause != null) throw new CloseException(cause);

                throw new CloseException("Failed to gratefully close the channel");
            }
        } catch (InterruptedException ex) {
            throw new CloseException(ex);
        }
    }

    /**
     * Protect the message. This method will
     * always return null if {@link #isEncrypted()} is
     * false
     *
     * @param message the message to protect
     * @return the protected message
     */
    @Override
    public byte[] protectMessage(final BaseMessage message) {
        if (pair == null || secret == null ||
                encodedSecret == null) return null;

        byte[] data = message.readAll();
        return encrypt(data);
    }

    /**
     * Encrypt the bytes using the connection
     * key
     *
     * @param message the message to protect
     * @return the protected message
     */
    private byte[] encrypt(final byte[] message) {
        try {
            Cipher cipher = Cipher.getInstance(SecureGen.SECRET_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secret);

            return cipher.doFinal(message);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                 | IllegalBlockSizeException | BadPaddingException ex) {
            ExceptionCollector.catchException(LocalConnection.class, ex);
        }

        return null;
    }
}
