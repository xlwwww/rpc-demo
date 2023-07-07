package test.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import test.client.RpcClientProxyManager;
import test.registry.ZkServiceRegistryImpl;

import java.util.List;

public class HelloServiceImplTest {
    @Autowired
    private ApplicationContext context;
    @Test
    public void testRegistry() {
        ZkServiceRegistryImpl impl = new ZkServiceRegistryImpl();
        impl.register("helloService", "127.0.0.1:9333");
        impl.register("helloService", "127.0.0.1:9334");
        List<String> helloService = impl.getServices("helloService");
        Assert.assertEquals(2, helloService.size());
    }

}