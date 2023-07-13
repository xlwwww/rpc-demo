package rpc.rpc.handler;

import io.netty.channel.*;

import lombok.extern.slf4j.Slf4j;

import rpc.rpc.msg.RpcRequest;
import rpc.rpc.msg.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static Map<String, CompletableFuture<Object>> requestFutureMap = new ConcurrentHashMap<>();

    private Channel channel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("client address = ", ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        String requestId = msg.getRequestId();
        CompletableFuture<Object> rpcResponseCompletableFuture = requestFutureMap.get(requestId);
        if (rpcResponseCompletableFuture != null) {
            requestFutureMap.remove(requestId);
            rpcResponseCompletableFuture.complete(msg.getResult());
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
    }

    public CompletableFuture sendRequest(RpcRequest request) {
        // 包装
        CompletableFuture<Object> future = new CompletableFuture<>();
        requestFutureMap.put(request.getRequestId(), future);
        channel.writeAndFlush(request);
        return future;
    }
}
