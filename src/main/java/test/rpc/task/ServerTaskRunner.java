package test.rpc.task;

import io.netty.channel.ChannelHandlerContext;
import test.rpc.msg.RpcRequest;

public interface ServerTaskRunner {
    void submit(ChannelHandlerContext ctx, RpcRequest request);
}
