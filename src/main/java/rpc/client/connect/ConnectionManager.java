package rpc.client.connect;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import rpc.lb.LoadBalancer;
import rpc.lb.LoadBalancerRR;
import rpc.rpc.handler.RpcClientHandler;
import rpc.registry.ServiceDiscovery;
import rpc.registry.ZkServiceRegistryImpl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ConnectionManager {
    public static final ConnectionManager connectionManager = new ConnectionManager();
    public static Map<String, RpcClientHandler> connectedNodes = new ConcurrentHashMap<>();
    private static final ServiceDiscovery serviceDiscovery = new ZkServiceRegistryImpl();
    private static final LoadBalancer loadBalancer = new LoadBalancerRR();
    private static final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);

    public static ConnectionManager getInstance() {
        return connectionManager;
    }

    public static RpcClientHandler connectNode(Class<?> service) throws InterruptedException {
        // 服务发现
        List<String> services = serviceDiscovery.getServices(service.getName());
        // 负载均衡
        String address = loadBalancer.select(services);
        // 从服务地址中解析主机名与端口号
        String[] array = StringUtils.split(address, ":");
        if (array == null || array.length < 2) {
            log.error("解析服务端地址错误");
        }
        // todo 这里没有做动态更新，可以在serviceDiscovery中添加listener实现
        if (connectedNodes.containsKey(address)) {
            return connectedNodes.get(address);
        }
        String host = array[0];
        int port = Integer.parseInt(array[1]);
        ChannelFuture sync = new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline cp = ch.pipeline();
                        cp.addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
                        cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
                        cp.addLast(new RpcClientHandler());
                    }
                })
                .connect(host, port).sync();
        if (sync.isSuccess()) {
            connectedNodes.put(address, new RpcClientHandler());
        } else {
            log.error("connect {} failed", address, sync.cause());
        }
        return connectedNodes.get(address);
    }
}
