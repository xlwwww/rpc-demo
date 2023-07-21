package rpc.rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import rpc.rpc.msg.MessageType;
import rpc.rpc.msg.RpcRequest;
import factory.ServerTaskFactory;
import rpc.rpc.msg.RpcResponse;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@io.netty.channel.ChannelHandler.Sharable
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("连接断开:" + ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        if (msg.getType() == MessageType.HEART_BEAT_REQUEST) {
            log.info("服务端接受PING,发送PONG");
            ctx.writeAndFlush(new RpcResponse(MessageType.HEART_BEAT_RESPONSE));
        }
        // todo 优化 一个server对应一个taskPool
        ServerTaskFactory.getServerTaskRunner().submit(ctx, msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 只会记录读idle
        if (evt instanceof IdleStateEvent) {
            log.info("超过丢失心跳的次数阈值，关闭连接");
            ctx.close();
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
