package test.service;

import test.rpc.annotations.RpcService;
// 代替手动配置
@RpcService(interfaceClass = HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "hello " + name;
    }
}
