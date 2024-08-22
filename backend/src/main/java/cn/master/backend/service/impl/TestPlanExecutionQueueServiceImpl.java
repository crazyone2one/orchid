package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.TestPlanExecutionQueue;
import cn.master.backend.mapper.TestPlanExecutionQueueMapper;
import cn.master.backend.service.TestPlanExecutionQueueService;
import org.springframework.stereotype.Service;

/**
 * 测试计划执行队列 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Service
public class TestPlanExecutionQueueServiceImpl extends ServiceImpl<TestPlanExecutionQueueMapper, TestPlanExecutionQueue> implements TestPlanExecutionQueueService {

}
