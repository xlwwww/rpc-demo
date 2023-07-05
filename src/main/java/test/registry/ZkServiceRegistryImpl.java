package test.registry;

import org.apache.curator.framework.CuratorFramework;
import test.util.CuratorUtil;

import java.net.InetSocketAddress;

public class ZkServiceRegistryImpl implements ServiceRegistry {
    @Override
    public void register(String rpcServiceName, String address) {
        String servicePath = CuratorUtil.PREFIX + "/" + rpcServiceName;
        CuratorFramework zkClient = CuratorUtil.getZkClient();
        CuratorUtil.createPersistentNode(servicePath, zkClient);
    }
}
