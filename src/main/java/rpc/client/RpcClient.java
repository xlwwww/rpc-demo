package rpc.client;

import codec.RpcDecoder;
import codec.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import factory.SingletonFactory;
import rpc.callback.MethodCallback;
import rpc.client.config.RpcConfig;
import rpc.client.invoke.AsyncInvokeStrategy;
import rpc.client.invoke.InvokeStrategy;
import rpc.client.invoke.SyncInvokeStrategy;
import rpc.future.RpcFuture;
import rpc.lb.LoadBalancer;
import rpc.lb.LoadBalancerRR;
import rpc.registry.ServiceDiscovery;
import rpc.registry.ZkServiceRegistryImpl;
import rpc.rpc.handler.RpcClientHandler;
import rpc.rpc.msg.RpcRequest;
import rpc.rpc.msg.RpcResponse;
import serializer.HessianSerializer;
import serializer.Serializer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class RpcClient {
    private EventLoopGroup eventLoopGroup;
    private Bootstrap bootstrap;
    private ServiceDiscovery serviceDiscovery; // 服务发现
    private LoadBalancer loadBalancer; // 负载均衡器

    public InvokeStrategy getInvokeStrategy() {
        return invokeStrategy;
    }

    public void setInvokeStrategy(InvokeStrategy invokeStrategy) {
        this.invokeStrategy = invokeStrategy;
    }

    private InvokeStrategy invokeStrategy; // invoker

    public RpcConfig getConfig() {
        return config;
    }

    public void setConfig(RpcConfig config) {
        this.config = config;
    }

    private RpcConfig config;

    private static ThreadPoolExecutor callBackThreadPool = new ThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));

    public ThreadPoolExecutor getCallBackThreadPool() {
        return callBackThreadPool;
    }

    public static final Map<String, RpcClientHandler> connectedNodes = new ConcurrentHashMap<>();
    private static final Serializer serializer = SingletonFactory.getInstance(HessianSerializer.class);

    public RpcClient(RpcConfig rpcConfig) {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline cp = ch.pipeline();
                        cp.addLast(new LoggingHandler(LogLevel.INFO));
                        cp.addLast(new IdleStateHandler(120, 30, 0, TimeUnit.SECONDS));
                        cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4));
                        cp.addLast(new RpcDecoder(serializer, RpcResponse.class));
                        cp.addLast(new RpcEncoder(serializer));
                        cp.addLast(new RpcClientHandler());
                    }
                });
        this.config = rpcConfig;
        this.serviceDiscovery = config.getServiceDiscoveryClazz() != null ?
                SingletonFactory.getInstance(config.getServiceDiscoveryClazz()) : new ZkServiceRegistryImpl();
        this.loadBalancer = config.getLoadBalancerClazz() != null ?
                SingletonFactory.getInstance(config.getLoadBalancerClazz()) : new LoadBalancerRR();
        this.invokeStrategy = buildInvokerStrategy(config.getInvokeStrategy());
    }


    private InvokeStrategy buildInvokerStrategy(String strategy) {
        // 异步
        if (strategy != null && strategy.equals(InvokeStrategy.FUTURE)) {
            return new AsyncInvokeStrategy(this);
        }
        // 同步
        return new SyncInvokeStrategy(this);
    }

    public RpcFuture sendRequest(RpcRequest request) {
        try {
            RpcClientHandler rpcClientHandler = connect(Class.forName(request.getInterfaceName()));
            if (rpcClientHandler == null) {
                return null;
            }
            return rpcClientHandler.sendRequest(request);
        } catch (ClassNotFoundException e) {
            log.error("sendRequest error", e);
        }
        return null;
    }

    public RpcClientHandler connect(Class<?> service) {
        // 服务发现
        List<String> services = serviceDiscovery.getServices(service.getName());
        // 负载均衡
        String address = loadBalancer.select(services);
        if (connectedNodes.containsKey(address)) {
            return connectedNodes.get(address);
        }
        // 从服务地址中解析主机名与端口号
        String[] array = StringUtils.split(address, ":");
        if (array == null || array.length < 2) {
            log.error("解析服务端地址错误");
            return null;
        }
        String host = array[0];
        int port = Integer.parseInt(array[1]);
        // todo 会不会有并发问题
        try {
            ChannelFuture sync = bootstrap.connect(host, port).sync();
            if (sync.isSuccess()) {
                connectedNodes.put(address, sync.channel().pipeline().get(RpcClientHandler.class));
            } else {
                log.error("connect {} failed", address, sync.cause());
            }
        } catch (Exception e) {
            log.error("connect sync exception", e);
        }
        return connectedNodes.get(address);
    }
}
