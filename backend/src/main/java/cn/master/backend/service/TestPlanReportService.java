package cn.master.backend.service;

import cn.master.backend.payload.dto.plan.TestPlanReportPostParam;
import cn.master.backend.payload.request.plan.TestPlanReportGenRequest;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.TestPlanReport;

import java.util.List;
import java.util.Map;

/**
 * 测试计划报告 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
public interface TestPlanReportService extends IService<TestPlanReport> {

    Map<String, String> genReportByExecution(String prepareReportId, TestPlanReportGenRequest request, String currentUser);

    void summaryGroupReport(String reportId);

    void summaryPlanReport(String reportId);

    void postHandleReport(TestPlanReportPostParam postParam, boolean useManual);

    void deleteByTestPlanIds(List<String> testPlanIds);
}
