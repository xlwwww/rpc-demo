package rpc.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import rpc.rpc.annotations.RpcService;
import rpc.registry.ServiceRegistry;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class RPCProviderBeanPostProcessor implements BeanPostProcessor {
    private static ServiceRegistry serviceRegistry;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            return bean;
        }
        RpcService rpcService = (RpcService) bean;
        // 服务注册
        try {
            serviceRegistry.register(rpcService.interfaceClass().getName(), InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return bean;
    }
}
