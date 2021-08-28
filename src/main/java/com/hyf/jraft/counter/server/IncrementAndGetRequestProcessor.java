package com.hyf.jraft.counter.server;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.entity.Task;
import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcRequestClosure;
import com.alipay.sofa.jraft.rpc.RpcRequestProcessor;
import com.google.protobuf.Message;
import com.hyf.jraft.counter.rpc.IncrementAndGetRequest;
import com.hyf.jraft.counter.rpc.ValueResponse;

import java.nio.ByteBuffer;

/**
 * @author baB_hyf
 * @date 2021/08/07
 */
public class IncrementAndGetRequestProcessor extends RpcRequestProcessor<IncrementAndGetRequest> {

    private CounterServer counterServer;

    public IncrementAndGetRequestProcessor(CounterServer counterServer) {
        super(null, IncrementAndGetRequest.getDefaultInstance());
        this.counterServer = counterServer;
    }

    @Override
    public Message processRequest(IncrementAndGetRequest request, RpcRequestClosure done) {

        // 写操作，非leader，跳转
        if (!counterServer.getNode().isLeader()) {
            return counterServer.redirect();
        }

        // 构建应答回调
        ValueResponse response = ValueResponse.getDefaultInstance();
        IncrementAndGetClosure closure = new IncrementAndGetClosure(counterServer, request, response, new Closure() {
            @Override
            public void run(Status status) {

                // 成功，返回ValueResponse应答
                if (status.isOk()) {
                    done.sendResponse(response);
                    return;
                }

                RpcContext rpcCtx = done.getRpcCtx();
                System.out.println(rpcCtx.getRemoteAddress());

                ValueResponse resp = ValueResponse.newBuilder(response)
                        .setSuccess(false)
                        .setErrMsg(status.getErrorMsg())
                        .build();

                done.sendResponse(resp);
            }
        });

        // 前提，当前节点为Leader节点
        // 将当前请求变成一个Task，放入当前节点的RingBuffer中，变成LogEntry发给其他节点
        Task task = new Task();
        task.setDone(closure);
        task.setData(ByteBuffer.wrap(request.toByteArray()));
        counterServer.getNode().apply(task);

        return null;
    }

    @Override
    public String interest() {
        return IncrementAndGetRequest.class.getName();
    }
}
