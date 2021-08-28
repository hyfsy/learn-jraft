package com.hyf.jraft;

import com.alipay.sofa.jraft.*;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.entity.Task;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.rpc.RaftRpcServerFactory;
import com.alipay.sofa.jraft.rpc.RpcServer;
import com.alipay.sofa.jraft.util.Endpoint;

/**
 * @author baB_hyf
 * @date 2021/08/07
 */
public class Server {

    public static void main(String[] args) {

        String groupId = "groupId";
        PeerId serverId = new PeerId("localhost", 8000);

        // Iterator
        // StateMachine
        // Node
        Node currentNode = RaftServiceFactory.createRaftNode(groupId, serverId);
        NodeOptions nodeOptions = new NodeOptions();
        nodeOptions.setLogUri("./log");
        nodeOptions.setRaftMetaUri("./metadata");
        nodeOptions.setInitialConf(JRaftUtils.getConfiguration("xxxx"));
        nodeOptions.setSnapshotUri("./snapshot"); // 可选，leader备份最新日志给宕机刚恢复的flower使用
        nodeOptions.setFsm(null); // 状态机
        boolean init = currentNode.init(nodeOptions);
        if (!init) {
            System.out.println("初始化失败");
            return;
        }

        Node createAndInitNode = RaftServiceFactory.createAndInitRaftNode(groupId, serverId, nodeOptions);

        // RPC service
        // 服务端

        // 本进程提供的 RPC 服务地址列表
        NodeManager.getInstance().addAddress(new Endpoint("RPC address", 0));
        RpcServer raftRpcServer = RaftRpcServerFactory.createRaftRpcServer(new Endpoint());
        System.out.println(raftRpcServer.boundPort());
        raftRpcServer.init(null);

        RaftRpcServerFactory.createAndStartRaftRpcServer(new Endpoint());

        // RaftGroupService 简化创建和启动一个 raft group 节点

        RaftGroupService raftGroupService = new RaftGroupService(groupId, serverId, nodeOptions);
        Node groupNode = raftGroupService.start();
        Task task = new Task();
        groupNode.apply(task);

        // snapshot

        groupNode.snapshot(null); // 手动触发快照
        groupNode.readIndex(null, null); // 线性一致读 RaftOptions.ReadOnlyOption 切换 lease read

    }

}
