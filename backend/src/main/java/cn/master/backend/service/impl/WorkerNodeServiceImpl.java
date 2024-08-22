package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.WorkerNode;
import cn.master.backend.mapper.WorkerNodeMapper;
import cn.master.backend.service.WorkerNodeService;
import org.springframework.stereotype.Service;

/**
 * DB WorkerID Assigner for UID Generator 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-20
 */
@Service
public class WorkerNodeServiceImpl extends ServiceImpl<WorkerNodeMapper, WorkerNode> implements WorkerNodeService {

}
