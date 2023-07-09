package rpc.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rpc.client.connect.ConnectionManager;
import rpc.client.connect.RpcFuture;
import rpc.lb.LoadBalancerRR;
import rpc.registry.ZkServiceRegistryImpl;
import rpc.rpc.handler.RpcClientHandler;
import rpc.rpc.msg.RpcRequest;
import rpc.test.example.HelloService;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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
        RpcClientHandler rpcClientHandler = ConnectionManager.connectNode(HelloService.class);
        // 发送请求
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setInterfaceName(HelloService.class.getName());
        Method hello = HelloService.class.getMethod("hello", String.class);
        request.setMethodName(hello.getName());
        request.setParameterTypes(hello.getParameterTypes());
        request.setParameters(new Object[]{"WANGXINLU"});
        RpcFuture rpcFuture = rpcClientHandler.sendRequest(request);
        System.out.println(rpcFuture.get());
    }

}