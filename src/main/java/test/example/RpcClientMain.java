package test.example;


import rpc.client.RpcClient;
import rpc.client.RpcProxy;


public class RpcClientMain {
    public static void main(String[] args) {
        // testRpc();
        testMultiRpc();
    }

    public static void testRpc() {
        HelloService helloService = RpcProxy.create(HelloService.class, new RpcClient());
        System.out.println(helloService.hello("wang"));
    }

    public static void testMultiRpc() {
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            new Thread(() -> {
                HelloService helloService = RpcProxy.create(HelloService.class, new RpcClient());
                System.out.println(helloService.hello("wang" + finalI));
            }).start();
        }
    }
}
