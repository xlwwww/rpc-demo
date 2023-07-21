package rpc.callback;

import rpc.callback.Callback;


public class MethodCallback<T> implements Callback<T>  {
    @Override
    public void onComplete(T t) {

    }

    @Override
    public void onError(Throwable throwable) {

    }
}
