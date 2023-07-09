package rpc.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CuratorUtil {
    private static CuratorFramework zkClient;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    public static final String PREFIX = "/rpc";

    public static List<String> getServiceAddresses(String path) {
        try {
            CuratorFramework zkClient = getZkClient();
            return zkClient.getChildren().forPath(PREFIX + path);
        } catch (Exception e) {
            log.error("GET persistent node for path [{}] fail", path);
        }
        return new ArrayList<>();
    }

    public static void createPersistentNode(String path, String address) {
        try {
            CuratorFramework zkClient = getZkClient();
            zkClient.create().creatingParentsIfNeeded().forPath(PREFIX + path + "/" + address);
        } catch (Exception e) {
            log.error("create persistent node for path [{}] fail", path);
        }
    }

    public static CuratorFramework getZkClient() {
        Properties properties = PropertiesFileUtil.readPropertiesFile("rpc.properties");
        String zookeeperAddress = properties.getProperty("rpc.zookeeper.address") != null ? properties.getProperty("rpc.zookeeper.address") : DEFAULT_ZOOKEEPER_ADDRESS;
        // if zkClient has been started, return directly
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        // Retry strategy. Retry 3 times, and will increase the sleep time between retries.
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        zkClient = CuratorFrameworkFactory.newClient(
                        zookeeperAddress,
                        5000,
                        3000,
                        retryPolicy);
        try {
            zkClient.start();
            // wait 30s until connect to the zookeeper
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("Time out waiting to connect to ZK!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;
    }
}
