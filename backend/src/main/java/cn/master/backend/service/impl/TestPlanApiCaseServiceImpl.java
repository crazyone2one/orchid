package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.TestPlanApiCase;
import cn.master.backend.mapper.TestPlanApiCaseMapper;
import cn.master.backend.service.TestPlanApiCaseService;
import org.springframework.stereotype.Service;

/**
 * 测试计划关联接口用例 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Service
public class TestPlanApiCaseServiceImpl extends ServiceImpl<TestPlanApiCaseMapper, TestPlanApiCase> implements TestPlanApiCaseService {

}
