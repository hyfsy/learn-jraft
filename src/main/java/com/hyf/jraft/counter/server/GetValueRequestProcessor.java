package com.hyf.jraft.counter.server;

import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.closure.ReadIndexClosure;
import com.alipay.sofa.jraft.rpc.RpcRequestClosure;
import com.alipay.sofa.jraft.rpc.RpcRequestProcessor;
import com.alipay.sofa.jraft.util.BytesUtil;
import com.google.protobuf.Message;
import com.hyf.jraft.counter.rpc.GetValueRequest;
import com.hyf.jraft.counter.rpc.ValueResponse;

/**
 * @author baB_hyf
 * @date 2021/08/07
 */
public class GetValueRequestProcessor extends RpcRequestProcessor<GetValueRequest> {

    private CounterServer counterServer;

    public GetValueRequestProcessor(CounterServer counterServer) {
        super(null, GetValueRequest.getDefaultInstance());
        this.counterServer = counterServer;
    }

    @Override
    public Message processRequest(GetValueRequest request, RpcRequestClosure done) {

        // 线性一致读操作，不区分leader/flower
        // if (!counterServer.getNode().isLeader()) {
        //     return counterServer.redirect();
        // }

        // 请求leader的当前logId（leader心跳检测确实是leader）
        // 同步当前LogEntry logId
        // return value
        counterServer.getNode().readIndex(BytesUtil.EMPTY_BYTES, new ReadIndexClosure() {
            @Override
            public void run(Status status, long index, byte[] reqCtx) {
                ValueResponse.Builder builder = ValueResponse.newBuilder();

                if (status.isOk()) {
                    // ReadIndexClosure 回调成功，可以从状态机读取最新数据返回
                    // 如果状态实现有版本概念，可以根据传入的日志 index 编号做读取。
                    builder.setSuccess(true)
                            .setCount(counterServer.getCounterStateMachine().getValue().get())
                            .build();
                }
                else {
                    // 特定情况下，比如发生选举，该读请求将失败
                    builder.setSuccess(false)
                            .setErrMsg("Fail to read value in read index mode")
                            .build();
                }

                done.sendResponse(builder.build());
            }
        });

        return null;
    }

    @Override
    public String interest() {
        return GetValueRequest.class.getName();
    }
}
