package test.client;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

@Slf4j
public class RpcClientProxyManager {
    /**
     * 使用： xxxservice. xxx  -> 实际是 proxy.sendRequest
     */
    public static <T> T create(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class[]{clazz},
                new ObjectProxy());
    }
}
