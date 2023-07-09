package rpc.rpc.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import rpc.rpc.msg.RpcRequest;
import rpc.rpc.msg.RpcResponse;
import rpc.client.connect.RpcFuture;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static Map<String, RpcFuture> requestFutureMap = new ConcurrentHashMap<>();

    private Channel channel;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        String requestId = msg.getRequestId();
        RpcFuture rpcFuture = requestFutureMap.get(requestId);
        if (rpcFuture != null) {
            requestFutureMap.remove(requestId);
            rpcFuture.done(msg);
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
    }

    public RpcFuture sendRequest(RpcRequest request) {
        // 包装
        RpcFuture future = new RpcFuture();
        requestFutureMap.put(request.getRequestId(), future);
        future.setId(request.getRequestId());
        channel.writeAndFlush(request);
        return future;
    }
}
