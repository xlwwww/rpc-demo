package spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import rpc.client.RpcClient;
import rpc.client.config.RpcConfig;
import rpc.client.proxy.RpcProxy;
import rpc.rpc.annotations.RpcReference;
import rpc.callback.MethodCallback;

import java.lang.reflect.Field;

@Slf4j
public class RPCConsumerBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 找到所有的field
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (!field.isAnnotationPresent(RpcReference.class)) {
                continue;
            }
            try {
                RpcConfig config =
                        RpcConfig.builder()
                                .invokeStrategy(field.getAnnotation(RpcReference.class).invokeStrategy())
                                .methodCallback(new MethodCallback<Object>() {
                                    @Override
                                    public void onComplete(Object o) {
                                        log.info("method complete callback,o={}", o);
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        log.error("method error,e=", throwable);
                                    }
                                })
                                .build();
                // 注入
                field.set(bean, RpcProxy.create(bean.getClass(), new RpcClient(config)));
                field.setAccessible(true);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return bean;
    }
}
