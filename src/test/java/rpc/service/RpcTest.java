package rpc.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rpc.client.RpcClient;
import rpc.client.config.RpcConfig;
import rpc.future.RpcFuture;
import rpc.lb.LoadBalancerRR;
import rpc.registry.ZkServiceRegistryImpl;
import rpc.callback.MethodCallback;
import rpc.rpc.msg.RpcRequest;
import test.example.HelloService;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:client-spring.xml"})
public class RpcTest {
    @Autowired
    private ApplicationContext context;

    @Test
    public void testRegistry() {
        ZkServiceRegistryImpl impl = new ZkServiceRegistryImpl();
        impl.register(HelloService.class.getName(), "127.0.0.1:9333");
        impl.register(HelloService.class.getName(), "127.0.0.1:9334");
        List<String> helloService = impl.getServices(HelloService.class.getName());
        Assert.assertEquals(2, helloService.size());

        // test loadbalancer
        LoadBalancerRR rr = new LoadBalancerRR();
        Assert.assertEquals("127.0.0.1:9333", rr.select(helloService));
        Assert.assertEquals("127.0.0.1:9334", rr.select(helloService));
    }

    @Test
    public void testConnectNode() throws InterruptedException, NoSuchMethodException, ExecutionException {
        RpcConfig config =
                RpcConfig.builder()
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
        // 发送请求
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setInterfaceName(HelloService.class.getName());
        Method hello = HelloService.class.getMethod("hello", String.class);
        request.setMethodName(hello.getName());
        request.setParameterTypes(hello.getParameterTypes());
        request.setParameters(new Object[]{"WANGXINLU"});
        RpcFuture rpcFuture = client.sendRequest(request);
        System.out.println(rpcFuture.get());
    }

}