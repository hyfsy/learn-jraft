package com.hyf.jraft.counter.server;

import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.RaftGroupService;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.rpc.RaftRpcServerFactory;
import com.alipay.sofa.jraft.rpc.RpcServer;
import com.alipay.sofa.jraft.rpc.impl.BoltRaftRpcFactory;
import com.hyf.jraft.counter.rpc.ValueResponse;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * java -jar server.jar C:\Users\baB_hyf\Desktop\jraft counter localhost:8111 localhost:8111,localhost:8112,localhost:8113
 * <p>
 * java -jar server.jar C:\Users\baB_hyf\Desktop\jraft2 counter localhost:8112 localhost:8111,localhost:8112,localhost:8113
 * <p>
 * java -jar server.jar C:\Users\baB_hyf\Desktop\jraft3 counter localhost:8113 localhost:8111,localhost:8112,localhost:8113
 *
 * @author baB_hyf
 * @date 2021/08/07
 */
public class CounterServer {

    private static final Logger log = LoggerFactory.getLogger(CounterServer.class);

    private RaftGroupService    raftGroupService;
    private CounterStateMachine counterStateMachine; // fsm
    private Node                node;

    public CounterServer(String dataPath, String groupId, PeerId serverId, NodeOptions nodeOptions) throws IOException {
        FileUtils.forceMkdir(new File(dataPath));

        BoltRaftRpcFactory boltRaftRpcFactory = new BoltRaftRpcFactory();

        // src\main>protoc -I proto/ --java_out=java/ counter.proto
        RpcServer rpcServer = boltRaftRpcFactory.createRpcServer(serverId.getEndpoint());
        rpcServer.registerProcessor(new GetValueRequestProcessor(this));
        rpcServer.registerProcessor(new IncrementAndGetRequestProcessor(this));
        // 添加JRaft内置的处理器
        RaftRpcServerFactory.addRaftRequestProcessors(rpcServer);

        this.counterStateMachine = new CounterStateMachine();
        nodeOptions.setFsm(this.counterStateMachine);

        // 设置存储路径
        nodeOptions.setLogUri(dataPath + File.separator + "data");
        nodeOptions.setRaftMetaUri(dataPath + File.separator + "meta");
        nodeOptions.setSnapshotUri(dataPath + File.separator + "snapshot");

        this.raftGroupService = new RaftGroupService(groupId, serverId, nodeOptions, rpcServer);
        this.node = this.raftGroupService.start();
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            log.error("Usage: java com.hyf.jraft.counter.server.CounterServer {dataPath} {groupId} {serverAddr} {initConfAddr}");
            return;
        }

        String dataPath = "C:\\Users\\baB_hyf\\Desktop\\jraft";
        String groupId = "counter";
        String serverAddr = "localhost:8111";
        String initConfAddr = "localhost:8111,localhost:8112,localhost:8113";
        dataPath = args[0];
        groupId = args[1];
        serverAddr = args[2];
        initConfAddr = args[3];

        PeerId server = new PeerId();
        if (!server.parse(serverAddr)) {
            log.error("服务器地址非法");
            return;
        }

        Configuration initConf = new Configuration();
        if (!initConf.parse(initConfAddr)) {
            log.error("集群地址非法");
            return;
        }

        NodeOptions nodeOptions = new NodeOptions();
        nodeOptions.setElectionTimeoutMs(5000);
        nodeOptions.setDisableCli(false);
        nodeOptions.setSnapshotIntervalSecs(30);
        nodeOptions.setInitialConf(initConf);

        CounterServer counterServer = new CounterServer(dataPath, groupId, server, nodeOptions);
        log.info("Start server at port: {}",
                counterServer.getNode().getNodeId().getPeerId().getPort());
    }

    public ValueResponse redirect() {
        ValueResponse.Builder builder = ValueResponse.newBuilder()
                .setSuccess(true);

        if (this.node != null) {
            PeerId leaderId = this.node.getLeaderId();
            if (leaderId != null) {
                builder.setRedirect(leaderId.toString());
            }
        }

        return builder.build();
    }

    public RaftGroupService getRaftGroupService() {
        return this.raftGroupService;
    }

    public CounterStateMachine getCounterStateMachine() {
        return this.counterStateMachine;
    }

    public Node getNode() {
        return this.node;
    }
}
