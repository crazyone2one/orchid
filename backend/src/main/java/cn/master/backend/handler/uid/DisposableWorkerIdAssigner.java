package cn.master.backend.handler.uid;

import cn.master.backend.constants.WorkerNodeType;
import cn.master.backend.entity.WorkerNode;
import cn.master.backend.mapper.WorkerNodeMapper;
import cn.master.backend.util.LogUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.redisson.api.RandomIdGenerator;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * @author Created by 11's papa on 08/20/2024
 **/
@Service
public class DisposableWorkerIdAssigner implements WorkerIdAssigner {
    @Resource
    private WorkerNodeMapper workerNodeMapper;
    @Override
    public long assignWorkerId() {
        try {
            WorkerNode workerNode = buildWorkerNode();

            // add worker node for new (ignore the same IP + PORT)
            workerNodeMapper.insert(workerNode);
            LogUtils.info("Add worker node:" + workerNode);

            return workerNode.getId();
        } catch (Exception e) {
            LogUtils.error("Assign worker id exception. ", e);
            return 1;
        }
    }

    private WorkerNode buildWorkerNode() {

        WorkerNode workerNode = new WorkerNode();
        workerNode.setType(WorkerNodeType.ACTUAL.value());
        workerNode.setHostName(NetUtils.getLocalAddress());
        workerNode.setPort(System.currentTimeMillis() + "-" + RandomUtils.nextInt());
        workerNode.setCreated(System.currentTimeMillis());
        workerNode.setModified(System.currentTimeMillis());
        workerNode.setLaunchDate(System.currentTimeMillis());
        return workerNode;
    }
}
