package com.hyf.jraft.counter.client;

import com.alipay.sofa.jraft.RaftServiceFactory;
import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.core.CliServiceImpl;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.rpc.CliClientService;
import com.alipay.sofa.jraft.rpc.RpcResponseClosureAdapter;
import com.hyf.jraft.counter.rpc.GetValueRequest;
import com.hyf.jraft.counter.rpc.IncrementAndGetRequest;
import com.hyf.jraft.counter.rpc.ValueResponse;
import com.hyf.jraft.counter.server.CounterServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

/**
 * java -jar client.jar counter localhost:8111,localhost:8112,localhost:8113
 *
 * @author baB_hyf
 * @date 2021/08/07
 */
public class CounterClient {

    private static final Logger log = LoggerFactory.getLogger(CounterServer.class);

    public static void main(String[] args) throws TimeoutException, InterruptedException {
        // if (args.length != 2) {
        //     log.error("Usage: java com.hyf.jraft.counter.server.CounterClient {groupId} {initConfAddr}");
        //     return;
        // }

        String groupId = "counter";
        String initConfAddr = "localhost:8111,localhost:8112,localhost:8113";
        // groupId = args[0];
        // initConfAddr = args[1];

        Configuration initConf = new Configuration();
        if (!initConf.parse(initConfAddr)) {
            log.error("集群地址非法");
            return;
        }

        // 更新raft group配置
        RouteTable.getInstance().updateConfiguration(groupId, initConf);

        // 初始化 RPC 客户端并更新路由表
        CliOptions cliOptions = new CliOptions();
        CliServiceImpl cliService = (CliServiceImpl) RaftServiceFactory.createAndInitCliService(cliOptions);
        CliClientService cliClientService = cliService.getCliClientService(); // 旧版本

        if (!RouteTable.getInstance().refreshConfiguration(cliClientService, groupId, 5000).isOk()) {
            log.error("Refresh leader failed");
            return;
        }

        // 获取 leader 后发送请求

        PeerId leader = RouteTable.getInstance().selectLeader(groupId);
        log.info("Leader is {}", leader);

        incrementAndGet(leader, cliClientService);

        Configuration configuration = RouteTable.getInstance().getConfiguration(groupId);
        PeerId peerId = configuration.getPeers().get(0);
        log.info("Get peer is {}", peerId);
        getValue(peerId, cliClientService);

        System.exit(0);
    }

    private static void incrementAndGet(PeerId leader, CliClientService cliClientService) throws InterruptedException {
        int n = 1000;
        CountDownLatch latch = new CountDownLatch(n);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            IncrementAndGetRequest request = IncrementAndGetRequest.newBuilder()
                    .setCount(i)
                    .build();

            int tempI = i;

            cliClientService.invokeWithDone(leader.getEndpoint(), request, new RpcResponseClosureAdapter<ValueResponse>() {
                @Override
                public void run(Status status) {
                    try {
                        ValueResponse response = getResponse();
                        log.info("Get response: {}, status: {}", response, status);
                        System.out.println("Get response: " + tempI + ", status: " + status);
                    } finally {
                        latch.countDown();
                    }
                }
            }, 5000);
        }
        latch.await();
        log.info("{} ops, cost {} ms.", n, System.currentTimeMillis() - start);
    }

    private static void getValue(PeerId peerId, CliClientService cliClientService) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        GetValueRequest request = GetValueRequest.getDefaultInstance();
        cliClientService.invokeWithDone(peerId.getEndpoint(), request, new RpcResponseClosureAdapter<ValueResponse>() {
            @Override
            public void run(Status status) {
                try {
                    if (status.isOk()) {
                        ValueResponse response = getResponse();
                        log.info("Get value: {}", response.getCount());
                        System.out.println("Get value: " + response.getCount());
                    }
                    else {
                        log.error("Fail to get value: {}", status);
                    }
                } finally {
                    latch.countDown();
                }
            }
        }, 3000);
        latch.await();
    }
}
