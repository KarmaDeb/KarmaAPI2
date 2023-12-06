package es.karmadev.api.netty.encoder;

import es.karmadev.api.netty.message.DecMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class DataDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        long id = byteBuf.readLong();
        byte[] data = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(data);

        DecMessage message = new DecMessage(id, data);
        out.add(message);
    }
}
