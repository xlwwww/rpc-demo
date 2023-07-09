package rpc.registry;

import rpc.util.CuratorUtil;

import java.util.List;

public class ZkServiceRegistryImpl implements ServiceRegistry, ServiceDiscovery {

    @Override
    public void register(String rpcServiceName, String address) {
        CuratorUtil.createPersistentNode(rpcServiceName, address);
    }

    @Override
    public List<String> getServices(String rpcServiceName) {
        return CuratorUtil.getServiceAddresses(rpcServiceName);
    }
}
