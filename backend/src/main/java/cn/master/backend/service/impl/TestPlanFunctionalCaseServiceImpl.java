package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.TestPlanFunctionalCase;
import cn.master.backend.mapper.TestPlanFunctionalCaseMapper;
import cn.master.backend.service.TestPlanFunctionalCaseService;
import org.springframework.stereotype.Service;

/**
 * 测试计划关联功能用例 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Service
public class TestPlanFunctionalCaseServiceImpl extends ServiceImpl<TestPlanFunctionalCaseMapper, TestPlanFunctionalCase> implements TestPlanFunctionalCaseService {

}
