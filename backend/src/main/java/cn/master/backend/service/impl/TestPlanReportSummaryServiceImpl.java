package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.TestPlanReportSummary;
import cn.master.backend.mapper.TestPlanReportSummaryMapper;
import cn.master.backend.service.TestPlanReportSummaryService;
import org.springframework.stereotype.Service;

/**
 * 测试计划报告内容统计 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Service
public class TestPlanReportSummaryServiceImpl extends ServiceImpl<TestPlanReportSummaryMapper, TestPlanReportSummary> implements TestPlanReportSummaryService {

}
