package com.hyf.jraft;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.JRaftUtils;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.closure.TaskClosure;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.entity.Task;
import com.alipay.sofa.jraft.error.RaftError;
import com.alipay.sofa.jraft.util.Endpoint;

import java.nio.ByteBuffer;

/**
 * @author baB_hyf
 * @date 2021/08/07
 */
public class Hello {

    public static void main(String[] args) {

        // 一个服务地址
        Endpoint localhost = new Endpoint("localhost", 8080);
        String s = localhost.toString();
        System.out.println(s);

        // raft 协议的参与者（leader/follower/candidate）
        PeerId peerId = new PeerId("localhost", 8080);
        peerId.parse(peerId.toString());
        Endpoint endpoint = peerId.getEndpoint();
        System.out.println(endpoint);

        // 一个 raft group 的配置
        Configuration configuration = new Configuration();
        configuration.addPeer(peerId);

        // 方便创建 Endpoint/PeerId/Configuration 等对象
        JRaftUtils.getEndPoint("localhost:8000");
        JRaftUtils.getPeerId("localhost:8000");
        JRaftUtils.getConfiguration("localhost:8081,localhost:8082,localhost:8083"); // 多个逗号隔开

        // 状态回调
        Closure done = new Closure() {

            @Override
            public void run(Status status) {
                int code = status.getCode();
                String errorMsg = status.getErrorMsg();
                // 枚举
                RaftError raftError = status.getRaftError();

                boolean ok = status.isOk();
                status.reset();
            }
        };
        // 请求状态
        Status ok = Status.OK();
        Status error_message = new Status(1000, "error message: %s", "test error");

        Task task = new Task();
        task.setExpectedTerm(1); // 任务提交时预期的 leader term，如果不提供(也就是默认值 -1 )，在任务应用到状态机之前不会检查 leader 是否发生了变更，如果提供了，那么在将任务应用到状态机之前，会检查 term 是否匹配，如果不匹配将拒绝该任务。
        task.setData(ByteBuffer.wrap("hello".getBytes())); // 业务数据
        task.setDone(done); // 任务回调

        TaskClosure taskClosure = new TaskClosure() {

            // 在任务完成的时候通知此对象，无论成功还是失败
            @Override
            public void run(Status status) {

            }

            // 日志复制到多数节点之后，应用到状态机之前调用
            @Override
            public void onCommitted() {
                System.out.println("");
            }
        };
    }

}
