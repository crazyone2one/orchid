package cn.master.backend.service;

import cn.master.backend.entity.TestPlan;
import cn.master.backend.payload.dto.plan.TestPlanResourceExecResultDTO;
import com.mybatisflex.core.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
public interface BaseTestPlanService extends IService<TestPlan> {
    void checkModule(String moduleId);

    Map<String, Map<String, List<String>>> parseExecResult(List<TestPlanResourceExecResultDTO> execResults);

    String calculateTestPlanStatus(List<String> resultList);

    String calculateStatusByChildren(List<String> childStatus);
}
