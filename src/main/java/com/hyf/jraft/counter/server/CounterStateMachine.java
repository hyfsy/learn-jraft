package com.hyf.jraft.counter.server;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Iterator;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.core.StateMachineAdapter;
import com.alipay.sofa.jraft.error.RaftError;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;
import com.alipay.sofa.jraft.util.Utils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hyf.jraft.counter.rpc.IncrementAndGetRequest;
import com.hyf.jraft.counter.rpc.ValueResponse;
import io.netty.handler.codec.CodecException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author baB_hyf
 * @date 2021/08/07
 */
public class CounterStateMachine extends StateMachineAdapter {

    private static final Logger log = LoggerFactory.getLogger(CounterStateMachine.class);

    private final AtomicBoolean isLeader = new AtomicBoolean(false);

    /**
     * counter value 当前节点的值
     */
    private AtomicLong value = new AtomicLong(0);

    @Override
    public void onLeaderStart(long term) {
        this.isLeader.set(true);
    }

    @Override
    public void onLeaderStop(Status status) {
        this.isLeader.set(false);
    }

    @Override
    public void onApply(Iterator iter) {
        while (iter.hasNext()) {
            long count = 0;

            IncrementAndGetClosure done = null;

            // done 回调不为null，必须在应用日志后调用，如果不为 null，说明当前是leader。
            if (iter.done() != null) {
                // 一点小优化
                // 当前是leader，可以直接从 IncrementAndAddClosure 中获取 delta，避免反序列化
                done = (IncrementAndGetClosure) iter.done();
                count = done.getRequest().getCount();
            }
            else {
                // 其他节点应用此日志，需要反序列化 IncrementAndGetRequest，获取 count
                try {
                    final ByteBuffer data = iter.getData();
                    // System.out.println("Data: " + data); // 奇怪的，有时候为null
                    if (data == null) {
                        long index = iter.getIndex();
                        long term = iter.getTerm();
                        System.out.println("Data is null, election term " + term + ", task index " + index);
                        iter.next();
                        continue;
                    }
                    IncrementAndGetRequest request = IncrementAndGetRequest.parseFrom(data.array());
                    count = request.getCount();
                } catch (CodecException | InvalidProtocolBufferException e) {
                    log.error("Fail to decode IncrementAndGetRequest", e);
                }
            }

            long prev = this.value.get();
            long updated = this.value.addAndGet(count);
            if (done != null) {
                // 更新状态机
                IncrementAndGetRequest request = done.getRequest();
                done.setRequest(request.toBuilder().setCount(updated).build());
                // 更新后，确保调用 done，返回应答给客户端。
                ValueResponse response = done.getResponse();
                done.setResponse(response.toBuilder().setSuccess(true).build());
                done.run(Status.OK());
            }

            log.info("Add value {} by count {} at logIndex {} and term {}",
                    prev, count, iter.getIndex(), iter.getTerm());
            iter.next();
        }
    }

    @Override
    public void onSnapshotSave(SnapshotWriter writer, Closure done) {

        // 获取此刻状态机状态
        long val = this.value.get();

        // 异步保存，避免阻塞状态机
        Utils.runInThread(() -> {
            CounterSnapshotFile snapshot = new CounterSnapshotFile(writer.getPath() + File.separator + "data");
            if (snapshot.save(val)) {
                if (writer.addFile("data")) {
                    done.run(Status.OK());
                }
                else {
                    done.run(new Status(RaftError.EIO, "Fail to add file to writer"));
                }
            }
            else {
                done.run(new Status(RaftError.EIO, "Fail to save counter snapshot %s", snapshot.getPath()));
            }
        });
    }

    @Override
    public boolean onSnapshotLoad(SnapshotReader reader) {
        // leader不用从 snapshot 加载，他不会接受 snapshot 安装请求
        if (isLeader.get()) {
            log.warn("Leader is not support to load snapshot");
            return true; // 返回false会报错
        }

        // 未找到数据文件，忽略
        if (reader.getFileMeta("data") == null) {
            log.error("Fail to find data file in {}", reader.getPath());
            return true; // 返回false会报错
        }

        // 将 snapshot 保存在 reader.getPath()/data 文件里
        CounterSnapshotFile snapshot = new CounterSnapshotFile(reader.getPath() + File.separator + "data");
        try {
            this.value.set(snapshot.load());
            return true;
        } catch (IOException e) {
            log.error("Fail to load snapshot from {}", snapshot.getPath());
            return false;
        }
    }

    public AtomicLong getValue() {
        return value;
    }
}
