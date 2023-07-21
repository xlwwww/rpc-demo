package rpc.callback;

public interface Callback<T> {
    void onComplete(T t);

    void onError(Throwable throwable);
}
