package rpc.rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import rpc.rpc.msg.RpcRequest;
import rpc.factory.ServerTaskFactory;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private AtomicInteger idleCount = new AtomicInteger(0);
    public static final int HEART_BEAT_TIME_OUT_MAX_TIME = 3;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        log.info("received msg");
        // reset 心跳
        idleCount.set(0);
        // 取handler处理，这里是io线程，切换业务线程
        ServerTaskFactory.getServerTaskRunner().submit(ctx, msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 只会记录读idle
        if (evt instanceof IdleStateEvent) {
            if (idleCount.incrementAndGet() >= HEART_BEAT_TIME_OUT_MAX_TIME) {
                log.info("超过丢失心跳的次数阈值，关闭连接");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
