package rpc.client;

import rpc.future.RpcFuture;


public class RpcContext {
    private static final ThreadLocal<RpcContext> LOCAL = new ThreadLocal<RpcContext>() {
        @Override
        protected RpcContext initialValue() {
            return new RpcContext();
        }
    };

    public RpcFuture getFuture() {
        return future;
    }

    public void setFuture(RpcFuture future) {
        this.future = future;
    }

    private RpcFuture future;

    public static RpcContext getContext() {
        return LOCAL.get();
    }
}
