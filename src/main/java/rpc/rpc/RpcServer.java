package rpc.rpc;

import codec.RpcDecoder;
import codec.RpcEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import factory.SingletonFactory;
import rpc.registry.ServiceRegistry;
import rpc.registry.ZkServiceRegistryImpl;
import rpc.rpc.handler.RpcServerHandler;
import rpc.rpc.annotations.RpcService;
import rpc.rpc.msg.RpcRequest;
import serializer.HessianSerializer;
import serializer.Serializer;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RpcServer implements ApplicationContextAware, InitializingBean {
    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    private String serverAddress;
    private ServiceRegistry serviceRegistry;

    // 维护interfaceName - serviceBean
    private static final Map<String, Object> services = new ConcurrentHashMap<>();
    private ApplicationContext applicationContext;

    public RpcServer(String serverAddress) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = new ZkServiceRegistryImpl();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(RpcService.class);
        for (Object o : beansWithAnnotation.values()) {
            RpcService rpcService = o.getClass().getAnnotation(RpcService.class);
            services.put(rpcService.interfaceClass().getName(), o);
            // 服务注册
            try {
                serviceRegistry.register(rpcService.interfaceClass().getName(), serverAddress);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Map<String, Object> getServices() {
        return services;
    }

    // afterPropertiesSet()
    @Override
    public void afterPropertiesSet() throws Exception {
        if (services.isEmpty()) {
            log.error("无服务可用，请检查配置");
            return;
        }
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        Serializer serializer = SingletonFactory.getInstance(HessianSerializer.class);
        // 开启服务器
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(boss, worker)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            // 心跳机制
                            // 会触发 IdleStateEvent 事件并且交给下一个 handler 处理，
                            // 下一个 handler 必须实现 userEventTriggered 方法处理对应事件
                            ch.pipeline().addLast(new IdleStateHandler(60, 0, 0));
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                            // 解决黏包 <header> -> <length> -> <data>
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4));
                            // bytebuf <-> message codec
                            ch.pipeline().addLast(new RpcDecoder(serializer, RpcRequest.class));
                            ch.pipeline().addLast(new RpcEncoder(serializer));
                            // rpc request handler
                            ch.pipeline().addLast(new RpcServerHandler());
                        }
                    });
            String[] array = serverAddress.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);
            ChannelFuture bind = serverBootstrap.bind(host, port);
            bind.sync();
            log.info("server started at {}", port);
            bind.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("sever started failed ,e =", e);
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }
}
