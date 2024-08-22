package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.TestPlanConfig;
import cn.master.backend.mapper.TestPlanConfigMapper;
import cn.master.backend.service.TestPlanConfigService;
import org.springframework.stereotype.Service;

/**
 * 测试计划配置 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Service
public class TestPlanConfigServiceImpl extends ServiceImpl<TestPlanConfigMapper, TestPlanConfig> implements TestPlanConfigService {

}
