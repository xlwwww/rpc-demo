package test.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import test.client.RpcClientProxyManager;

import test.rpc.annotations.RpcReference;

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
                // 注入
                field.set(bean, RpcClientProxyManager.create(bean.getClass()));
                field.setAccessible(true);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return bean;
    }
}
