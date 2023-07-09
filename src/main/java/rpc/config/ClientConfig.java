package rpc.config;

import lombok.Builder;
import lombok.Data;
import rpc.lb.LoadBalancer;
import rpc.registry.ServiceDiscovery;

@Data
@Builder
public class ClientConfig {
    private ServiceDiscovery serviceDiscovery;
    private LoadBalancer loadBalancer;
}
