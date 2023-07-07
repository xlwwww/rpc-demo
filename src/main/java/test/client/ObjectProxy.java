package test.client;

import test.client.connect.ConnectionManager;
import test.client.connect.RpcFuture;
import test.rpc.handler.RpcClientHandler;
import test.rpc.msg.RpcRequest;
import test.rpc.msg.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;

public class ObjectProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcClientHandler rpcClientHandler = ConnectionManager.connectNode(proxy.getClass());
        // 发送请求
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setInterfaceName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        RpcFuture rpcFuture = rpcClientHandler.sendRequest(request);
        return rpcFuture.get();
    }

}
