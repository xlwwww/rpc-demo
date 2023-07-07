package test.registry;

import java.util.List;

public interface ServiceDiscovery {
    List<String> getServices(String rpcServiceName);
}
