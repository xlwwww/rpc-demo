package rpc.rpc.task;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import rpc.rpc.RpcServer;
import rpc.rpc.msg.RpcRequest;
import rpc.rpc.msg.RpcResponse;
import rpc.rpc.annotations.RpcService;

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
            Object o = RpcServer.getServices().get(request.getInterfaceName());
            try {
                log.info("处理业务逻辑，request = {}", request.getRequestId());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Method method = o.getClass().getMethod(request.getMethodName(), request.getParameterTypes());
                Object result = method.invoke(o, request.getParameters());
                // write rpc response
                ctx.writeAndFlush(new RpcResponse(request.getRequestId(), result))
                        .addListener(future -> {
                            if (future.isSuccess()) {
                                log.info("successfully write rpc response,channel ={}", ctx.channel().toString());
                            } else {
                                log.error("write rpc response failed,channel = {},e ={}", ctx.channel().toString(), future.cause());
                            }
                        });
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
                ctx.writeAndFlush(new RpcResponse(request.getRequestId(), e));
            }
        });
    }
}
