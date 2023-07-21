package rpc.client.invoke;

import rpc.callback.MethodCallback;
import rpc.client.RpcClient;
import rpc.future.RpcFuture;
import rpc.rpc.msg.RpcRequest;

import java.util.concurrent.ExecutionException;

public class SyncInvokeStrategy extends InvokeStrategy {
    public SyncInvokeStrategy(RpcClient client) {
        super(client);
    }

    @Override
    public Object invoke(RpcRequest request, MethodCallback callback) {
        RpcFuture rpcFuture = client.sendRequest(request);
        if (rpcFuture == null) {
            return null;
        }
        // 不要使用whenComplete,会跑在netty线程上，回调是跑在客户端的
        rpcFuture.whenCompleteAsync((result, exception) -> {
            if (result != null) {
                callback.onComplete(result);
            } else {
                callback.onError((Throwable) exception);
            }
        }, client.getCallBackThreadPool());
        try {
            return rpcFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
