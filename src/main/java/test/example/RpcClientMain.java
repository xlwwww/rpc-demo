package test.example;


import lombok.extern.slf4j.Slf4j;
import rpc.client.invoke.InvokeStrategy;
import rpc.client.RpcContext;
import rpc.client.RpcClient;
import rpc.client.config.RpcConfig;
import rpc.client.proxy.RpcProxy;
import rpc.lb.LoadBalancerRR;
import rpc.registry.ZkServiceRegistryImpl;
import rpc.callback.MethodCallback;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class RpcClientMain {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        testRpc();
        testMultiRpc();
        testAsyncRpc();
        testMultiAsyncRpc();
    }


    public static void testMultiAsyncRpc() {
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            new Thread(() -> {
                RpcConfig config =
                        RpcConfig.builder()
                                .invokeStrategy(InvokeStrategy.FUTURE)
                                .methodCallback(new MethodCallback<Object>() {
                                    @Override
                                    public void onComplete(Object o) {
                                        log.info("method complete,o={}", o);
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        log.error("method error,e=", throwable);
                                    }
                                })
                                .build();
                RpcClient client = new RpcClient(config);
                // 异步方法
                HelloService helloService = RpcProxy.create(HelloService.class, client);
                System.out.println("获取future " + helloService.hello("WANG" + finalI));
                try {
                    System.out.println("获取结果 :" + RpcContext.getContext().getFuture().get(10, TimeUnit.SECONDS));
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    log.error("future get 异常，", e);
                }
            }).start();
        }
    }

    public static void testAsyncRpc() throws ExecutionException, InterruptedException {
        RpcConfig config =
                RpcConfig.builder()
                        .invokeStrategy(InvokeStrategy.FUTURE)
                        .methodCallback(new MethodCallback<Object>() {
                            @Override
                            public void onComplete(Object o) {
                                log.info("method complete,o={}", o);
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                log.error("method error,e=", throwable);
                            }
                        })
                        .build();
        RpcClient client = new RpcClient(config);
        // 异步方法
        HelloService helloService = RpcProxy.create(HelloService.class, client);
        System.out.println("获取future " + helloService.hello("WANG"));
        // 从threadlocal中拿到future
        System.out.println("获取结果 :" + RpcContext.getContext().getFuture().get());
    }

    public static void testRpc() {
        RpcConfig config =
                RpcConfig.builder()
                        .serviceDiscoveryClazz(ZkServiceRegistryImpl.class)
                        .loadBalancerClazz(LoadBalancerRR.class)
                        .methodCallback(new MethodCallback<Object>() {
                            @Override
                            public void onComplete(Object o) {
                                log.info("method complete,o={}", o);
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                log.error("method error,e=", throwable);
                            }
                        })
                        .build();
        RpcClient client = new RpcClient(config);
        // 同步方法
        HelloService helloService = RpcProxy.create(HelloService.class, client);
        System.out.println(helloService.hello("WANG"));
    }

    public static void testMultiRpc() {
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            new Thread(() -> {
                RpcConfig config =
                        RpcConfig.builder()
                                .serviceDiscoveryClazz(ZkServiceRegistryImpl.class)
                                .loadBalancerClazz(LoadBalancerRR.class)
                                .methodCallback(new MethodCallback<Object>() {
                                    @Override
                                    public void onComplete(Object o) {
                                        log.info("method complete,o={}", o);
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        log.error("method error,e=", throwable);
                                    }
                                })
                                .build();
                RpcClient client = new RpcClient(config);
                HelloService helloService = RpcProxy.create(HelloService.class, client);
                System.out.println(helloService.hello("wang" + finalI));
            }).start();
        }
    }
}
