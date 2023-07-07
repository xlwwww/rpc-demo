package test.rpc.task;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import test.rpc.RpcServer;
import test.rpc.msg.RpcRequest;
import test.rpc.msg.RpcResponse;
import test.rpc.annotations.RpcService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

@Slf4j
public class ServerTaskRunnerImpl implements ServerTaskRunner {
    private static ExecutorService executorService = new ForkJoinPool();

    @Override
    public void submit(ChannelHandlerContext ctx, RpcRequest request) {
        executorService.submit(() -> {
            RpcService o = (RpcService) RpcServer.getServices().get(request.getInterfaceName());
            try {
                Method method = o.interfaceClass().getMethod(request.getMethodName(), request.getParameterTypes());
                Object result = method.invoke(o, request.getParameters());
                // write rpc response
                ctx.writeAndFlush(new RpcResponse(result));
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
                ctx.writeAndFlush(new RpcResponse(e));
            }
        });
    }
}
