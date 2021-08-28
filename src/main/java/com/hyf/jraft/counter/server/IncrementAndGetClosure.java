package com.hyf.jraft.counter.server;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Status;
import com.hyf.jraft.counter.rpc.IncrementAndGetRequest;
import com.hyf.jraft.counter.rpc.ValueResponse;

/**
 * @author baB_hyf
 * @date 2021/08/07
 */
public class IncrementAndGetClosure implements Closure {

    private CounterServer          counterServer;
    private IncrementAndGetRequest request;
    private ValueResponse          response;
    private Closure                done; // 网络应答callback

    public IncrementAndGetClosure(CounterServer counterServer, IncrementAndGetRequest request, ValueResponse response, Closure done) {
        this.counterServer = counterServer;
        this.request = request;
        this.response = response;
        this.done = done;
    }

    @Override
    public void run(Status status) {
        // 返回应答给客户端
        if (this.done != null) {
            done.run(status);
        }
    }

    public IncrementAndGetRequest getRequest() {
        return request;
    }

    public void setRequest(IncrementAndGetRequest request) {
        this.request = request;
    }

    public ValueResponse getResponse() {
        return response;
    }

    public void setResponse(ValueResponse response) {
        this.response = response;
    }
}
