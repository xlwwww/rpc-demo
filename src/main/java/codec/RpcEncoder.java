package codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import serializer.Serializer;

public class RpcEncoder extends MessageToByteEncoder {
    private Serializer serializer;

    public RpcEncoder(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object t, ByteBuf byteBuf) throws Exception {
        byte[] serialize = serializer.serialize(t);
        byteBuf.writeInt(serialize.length);
        byteBuf.writeBytes(serialize);
    }
}
