package test.rpc.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * server使用 @RpcService 代替手动配置
 */
@Target(ElementType.TYPE) // 表示 @RpcService 注解可放在 接口、类、枚举、注解 上
@Retention(RetentionPolicy.RUNTIME) // 表示 @RpcService 注解会在class字节码文件中存在，在运行时可以通过反射获取到
@Component
public @interface RpcService {
    // 服务类型
    Class<?> interfaceClass();
}
