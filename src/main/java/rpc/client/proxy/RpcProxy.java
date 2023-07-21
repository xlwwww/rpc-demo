package rpc.client.proxy;

import rpc.client.RpcClient;

import java.lang.reflect.Proxy;

public class RpcProxy {
    /**
     * 使用： xxxservice. xxx  -> 实际是 proxy.sendRequest
     */
    public static <T> T create(Class<T> clazz, RpcClient client) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class[]{clazz},
                new ObjectProxy(client));
    }
}
