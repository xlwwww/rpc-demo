package test.registry;

import java.net.InetSocketAddress;

public interface ServiceRegistry {
    void register(String rpcServiceName, String address);
}
