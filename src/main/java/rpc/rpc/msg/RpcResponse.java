package rpc.rpc.msg;

import lombok.Data;

@Data
public class RpcResponse {
    public RpcResponse(Object result) {
        this.result = result;
    }
    public RpcResponse(Exception exception) {
        this.exception = exception;
    }
    private String requestId; // 表示对该 requestId 的请求进行响应
    private Exception exception;
    private Object result;

}
