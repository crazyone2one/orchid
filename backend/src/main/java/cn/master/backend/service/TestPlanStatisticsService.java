package cn.master.backend.service;

import cn.master.backend.payload.response.plan.TestPlanDetailResponse;
import cn.master.backend.payload.response.plan.TestPlanStatisticsResponse;

import java.util.List;

/**
 * @author Created by 11's papa on 08/20/2024
 **/
public interface TestPlanStatisticsService {
    List<TestPlanStatisticsResponse> calculateRate(List<String> paramIds);

    void calculateCaseCount(List<TestPlanDetailResponse> response);
}
