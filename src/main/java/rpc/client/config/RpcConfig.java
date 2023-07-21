package rpc.client.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import rpc.callback.MethodCallback;
import rpc.client.invoke.InvokeStrategy;
import rpc.lb.LoadBalancer;
import rpc.registry.ServiceDiscovery;

@Builder
@AllArgsConstructor
public class RpcConfig {
    public MethodCallback<Object> getMethodCallback() {
        return methodCallback;
    }

    public void setMethodCallback(MethodCallback<Object> methodCallback) {
        this.methodCallback = methodCallback;
    }

    private MethodCallback<Object> methodCallback;

    private Class<? extends ServiceDiscovery> serviceDiscoveryClazz;

    public Class<? extends ServiceDiscovery> getServiceDiscoveryClazz() {
        return serviceDiscoveryClazz;
    }

    public void setServiceDiscoveryClazz(Class<? extends ServiceDiscovery> serviceDiscoveryClazz) {
        this.serviceDiscoveryClazz = serviceDiscoveryClazz;
    }

    public Class<? extends LoadBalancer> getLoadBalancerClazz() {
        return loadBalancerClazz;
    }

    public void setLoadBalancerClazz(Class<? extends LoadBalancer> loadBalancerClazz) {
        this.loadBalancerClazz = loadBalancerClazz;
    }

    private Class<? extends LoadBalancer> loadBalancerClazz;


    public String getInvokeStrategy() {
        return invokeStrategy;
    }

    public void setInvokeStrategy(String invokeStrategy) {
        this.invokeStrategy = invokeStrategy;
    }

    private String invokeStrategy;
}
