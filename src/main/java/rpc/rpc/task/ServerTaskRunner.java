package rpc.rpc.task;

import io.netty.channel.ChannelHandlerContext;
import rpc.rpc.msg.RpcRequest;

public interface ServerTaskRunner {
    void submit(ChannelHandlerContext ctx, RpcRequest request);
}
