package rpc.rpc.msg;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpcResponse implements Serializable {
    public RpcResponse(String requestId, Object result) {
        this.requestId = requestId;
        this.result = result;
    }

    public RpcResponse(String requestId,Exception exception) {
        this.requestId = requestId;
        this.exception = exception;
    }

    private String requestId; // 表示对该 requestId 的请求进行响应
    private Exception exception;
    private Object result;

}
