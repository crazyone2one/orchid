package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.TestPlanCollection;
import cn.master.backend.mapper.TestPlanCollectionMapper;
import cn.master.backend.service.TestPlanCollectionService;
import org.springframework.stereotype.Service;

/**
 * 测试集 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Service
public class TestPlanCollectionServiceImpl extends ServiceImpl<TestPlanCollectionMapper, TestPlanCollection> implements TestPlanCollectionService {

}
