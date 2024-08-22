package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.TestPlanApiScenario;
import cn.master.backend.mapper.TestPlanApiScenarioMapper;
import cn.master.backend.service.TestPlanApiScenarioService;
import org.springframework.stereotype.Service;

/**
 * 测试计划关联场景用例 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Service
public class TestPlanApiScenarioServiceImpl extends ServiceImpl<TestPlanApiScenarioMapper, TestPlanApiScenario> implements TestPlanApiScenarioService {

}
