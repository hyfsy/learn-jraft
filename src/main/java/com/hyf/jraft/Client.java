package com.hyf.jraft;

import com.alipay.sofa.jraft.CliService;
import com.alipay.sofa.jraft.RaftServiceFactory;
import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.rpc.CliClientService;
import com.alipay.sofa.jraft.rpc.RpcClient;
import com.alipay.sofa.jraft.rpc.impl.BoltRaftRpcFactory;
import com.alipay.sofa.jraft.rpc.impl.cli.CliClientServiceImpl;

import java.util.concurrent.TimeoutException;

/**
 * @author baB_hyf
 * @date 2021/08/07
 */
public class Client {
    
    public static void main(String[] args) throws TimeoutException, InterruptedException {
        String groupId = "groupId";
        
        // 集群节点配置
        Configuration configuration = new Configuration();
        
        BoltRaftRpcFactory boltRaftRpcFactory = new BoltRaftRpcFactory();
        
        RpcClient rpcClient = boltRaftRpcFactory.createRpcClient();
        rpcClient.init(new CliOptions());
        
        // 初始化 RPC 服务 CliClientService
        CliClientService cliClientService = new CliClientServiceImpl();
        
        // 更新路由表配置
        RouteTable routeTable = RouteTable.getInstance();
        routeTable.updateConfiguration(groupId, configuration);
        
        // 刷新 leader 信息，超时 10 秒，客户端需要定期刷新获取集群最新状态
        boolean ok = routeTable.refreshLeader(cliClientService, groupId, 5000).isOk();
        if (ok) {
            // 获取集群 leader 节点，未知则为 null
            PeerId leader = routeTable.selectLeader(groupId);
        }
        
        // Cli 服务
        
        CliOptions cliOptions = new CliOptions();
        CliService cliService = RaftServiceFactory.createAndInitCliService(cliOptions);
        Status status = cliService.addPeer(groupId, configuration, new PeerId()); // 最终都转发到leader上执行
        if (status.isOk()) {
            System.out.println("添加节点成功");
        }
        
        // 强制设定剩余存活节点的配置，丢弃故障节点，让剩余节点尽快选举出新的 leader，代价可能是丢失数据，失去一致性承诺
        // 只有在非常紧急并且可用性更为重要的情况下使用
        cliService.resetPeer(groupId, PeerId.emptyPeer(), configuration);
    }
    
}
