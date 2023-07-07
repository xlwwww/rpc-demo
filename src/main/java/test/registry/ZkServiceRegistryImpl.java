package test.registry;

import test.util.CuratorUtil;

import java.util.List;

public class ZkServiceRegistryImpl implements ServiceRegistry, ServiceDiscovery {

    @Override
    public void register(String rpcServiceName, String address) {
        String servicePath = CuratorUtil.PREFIX + "/" + rpcServiceName;
        CuratorUtil.createPersistentNode(servicePath, address);
    }

    @Override
    public List<String> getServices(String rpcServiceName) {
        return CuratorUtil.getServiceAddresses(rpcServiceName);
    }
}
