package test.rpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import test.rpc.handler.RpcServerHandler;
import test.rpc.service.RpcService;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

public class RpcServer implements ApplicationContextAware, InitializingBean {
    // 维护interfaceName - serviceBean
    private static Map<String, Object> services = new HashMap<>();
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(beansWithAnnotation)) {
            for (String name : beansWithAnnotation.keySet()) {
                RpcService o = (RpcService) beansWithAnnotation.get(name);
                services.put(o.interfaceClass().getName(), o);
            }
        }
    }

    // afterPropertiesSet() -> @postConstruct
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
                            // 解决黏包
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 12, 4));
                            // message codec
                            // rpc request handler
                            ch.pipeline().addLast(new RpcServerHandler());
                        }
                    }).bind().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
