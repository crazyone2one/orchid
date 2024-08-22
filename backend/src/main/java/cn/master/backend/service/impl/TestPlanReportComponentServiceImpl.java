package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.TestPlanReportComponent;
import cn.master.backend.mapper.TestPlanReportComponentMapper;
import cn.master.backend.service.TestPlanReportComponentService;
import org.springframework.stereotype.Service;

/**
 * 测试计划报告逐组件表 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Service
public class TestPlanReportComponentServiceImpl extends ServiceImpl<TestPlanReportComponentMapper, TestPlanReportComponent> implements TestPlanReportComponentService {

}
