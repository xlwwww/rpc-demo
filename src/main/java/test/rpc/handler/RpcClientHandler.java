package test.rpc.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import test.client.connect.ConnectionManager;
import test.client.connect.RpcFuture;
import test.rpc.msg.RpcRequest;
import test.rpc.msg.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

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
