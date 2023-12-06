package es.karmadev.api.netty.encoder;

import es.karmadev.api.channel.com.IConnection;
import es.karmadev.api.channel.data.BaseMessage;
import es.karmadev.api.netty.message.nat.Messages;
import es.karmadev.api.object.var.Variable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public final class DataEncoder extends MessageToByteEncoder<BaseMessage> {

    private final Variable<IConnection> connection;

    public DataEncoder(final Variable<IConnection> connection) {
        this.connection = connection;
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final BaseMessage message, final ByteBuf out) {
        IConnection element = connection.get();
        if (message.getId() != Messages.KEY_EXCHANGE.getId() && element != null && element.isEncrypted()) {
            byte[] encoded = element.protectMessage(message);

            if (encoded != null) {
                out.writeBytes(encoded);
                return;
            }
        }

        out.writeLong(message.getId());
        out.writeBytes(message.readAll());
    }
}
