package rpc.rpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import rpc.rpc.handler.RpcServerHandler;
import rpc.rpc.annotations.RpcService;

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

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    private String serverAddress;
    private String registryAddress;

    // 维护interfaceName - serviceBean
    private static Map<String, Object> services = new ConcurrentHashMap<>();
    private ApplicationContext applicationContext;

    public RpcServer(String serverAddress, String registryAddress) {
        this.serverAddress = serverAddress;
        this.registryAddress = registryAddress;
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
        // 开启服务器
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(boss, worker)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            // 心跳机制
                            // 会触发 IdleStateEvent 事件并且交给下一个 handler 处理，
                            // 下一个 handler 必须实现 userEventTriggered 方法处理对应事件
                            ch.pipeline().addLast(new IdleStateHandler(60, 0, 0));
                            // 解决黏包 <header> -> <length> -> <data>
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 12, 4));
                            // bytebuf <-> message codec
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
