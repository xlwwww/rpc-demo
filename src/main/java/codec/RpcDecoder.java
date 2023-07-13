package codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import serializer.Serializer;

import java.util.List;

@Slf4j
public class RpcDecoder extends ByteToMessageDecoder {
    private Serializer serializer;
    private Class clazz;

    public RpcDecoder(Serializer serializer, Class<?> clazz) {
        this.serializer = serializer;
        this.clazz = clazz;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        int len = byteBuf.readInt();
        byteBuf.markReaderIndex();
        if (byteBuf.readableBytes() < len) {
            byteBuf.resetReaderIndex();
            return;
        }
        byte[] data = new byte[len];
        byteBuf.readBytes(data);
        Object obj = null;
        try {
            obj = serializer.deserialize(data, clazz);
            list.add(obj);
        } catch (Exception ex) {
            log.error("Decode error: " + ex.toString());
        }
    }
}
