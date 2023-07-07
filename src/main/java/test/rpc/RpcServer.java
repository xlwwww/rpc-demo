package test.rpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
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
import test.rpc.handler.RpcServerHandler;
import test.rpc.annotations.RpcService;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcServer implements ApplicationContextAware, InitializingBean {
    // 维护interfaceName - serviceBean
    private static Map<String, Object> services = new ConcurrentHashMap<>();
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(RpcService.class);
        for (String key : beansWithAnnotation.keySet()) {
            RpcService o = (RpcService) beansWithAnnotation.get(key);
            services.put(o.interfaceClass().getName(), o);
        }
    }

    public static Map<String, Object> getServices() {
        return services;
    }

    // afterPropertiesSet()
    @Override
    public void afterPropertiesSet() throws Exception {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        // 开启服务器
        try {
            ChannelFuture bind = new ServerBootstrap()
                    .group(boss, worker)
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
                    }).bind().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
