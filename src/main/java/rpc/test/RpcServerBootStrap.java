package rpc.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RpcServerBootStrap {
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("client-spring.xml");
    }
}
