package com.rpc.common.bean;

/**
 * @Description: 封装rpc响应格式
 * @Author: Jingzeng Wang
 * @Date: Created in 10:56  2017/8/21.
 */
public class RpcResponse {
    private String requestId;
    private Exception exception;
    private Object result;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
