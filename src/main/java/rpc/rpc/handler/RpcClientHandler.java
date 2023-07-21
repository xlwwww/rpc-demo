package rpc.rpc.handler;

import io.netty.channel.*;

import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import rpc.callback.MethodCallback;
import rpc.future.RpcFuture;
import rpc.rpc.msg.MessageType;
import rpc.rpc.msg.RpcRequest;
import rpc.rpc.msg.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static Map<String, RpcFuture<Object>> requestFutureMap = new ConcurrentHashMap<>();

    private Channel channel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("client address = ", ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        if (msg.getType() == MessageType.HEART_BEAT_RESPONSE) {
            //PONG
            log.info("收到服务端心跳");
            return;
        }
        String requestId = msg.getRequestId();
        RpcFuture<Object> rpcResponseCompletableFuture = requestFutureMap.get(requestId);
        if (rpcResponseCompletableFuture != null) {
            requestFutureMap.remove(requestId);
            rpcResponseCompletableFuture.complete(msg.getResult());
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.info("通道关闭");

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.WRITER_IDLE) {
                // 60s写空闲，定时PING -> 服务端回应PONG
                sendHeartBeat();
            }
        }
    }

    public RpcFuture sendRequest(RpcRequest request) {
        // 包装
        RpcFuture<Object> future = new RpcFuture<>();
        requestFutureMap.put(request.getRequestId(), future);
        channel.writeAndFlush(request);
        return future;
    }

    public void sendHeartBeat() {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setType(MessageType.HEART_BEAT_REQUEST);
        channel.writeAndFlush(rpcRequest);
    }
}
