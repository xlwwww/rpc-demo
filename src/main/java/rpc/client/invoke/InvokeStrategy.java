package rpc.client.invoke;

import rpc.callback.MethodCallback;
import rpc.client.RpcClient;
import rpc.rpc.msg.RpcRequest;


public abstract class InvokeStrategy {
    protected RpcClient client;
    public static final String SYNC = "SYNC";
    public static final String FUTURE = "FUTURE";

    public InvokeStrategy(RpcClient client) {
        this.client = client;
    }


    // sync invoke
    public abstract Object invoke(RpcRequest request, MethodCallback callback);
}
