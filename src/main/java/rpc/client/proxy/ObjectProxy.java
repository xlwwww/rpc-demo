package rpc.client.proxy;

import rpc.client.RpcClient;
import rpc.client.RpcContext;
import rpc.future.RpcFuture;
import rpc.rpc.msg.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

public class ObjectProxy implements InvocationHandler {
    private final RpcClient client;

    public ObjectProxy(RpcClient client) {
        this.client = client;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 发送请求
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setInterfaceName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        return client.getInvokeStrategy().invoke(request, client.getConfig().getMethodCallback());
    }

}
