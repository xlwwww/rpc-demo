package test.config;

import lombok.Builder;
import lombok.Data;
import test.lb.LoadBalancer;
import test.registry.ServiceDiscovery;

@Data
@Builder
public class ClientConfig {
    private ServiceDiscovery serviceDiscovery;
    private LoadBalancer loadBalancer;
}
