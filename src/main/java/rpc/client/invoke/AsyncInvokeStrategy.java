package rpc.client.invoke;

import rpc.callback.MethodCallback;
import rpc.client.RpcClient;
import rpc.client.RpcContext;
import rpc.future.RpcFuture;
import rpc.rpc.msg.RpcRequest;

public class AsyncInvokeStrategy extends InvokeStrategy {

    public AsyncInvokeStrategy(RpcClient client) {
        super(client);
    }

    @Override
    public Object invoke(RpcRequest request, MethodCallback callback) {
        // 这个future是等待结果线程，同步使用
        RpcFuture rpcFuture = client.sendRequest(request);
        RpcContext.getContext().setFuture(rpcFuture);
        // 不要使用whenComplete,会跑在netty线程上，回调是跑在客户端的
        rpcFuture.whenCompleteAsync((result, exception) -> {
            if (result != null) {
                callback.onComplete(result);
            } else {
                callback.onError((Throwable) exception);
            }
        }, client.getCallBackThreadPool());
        return null;
    }
}
