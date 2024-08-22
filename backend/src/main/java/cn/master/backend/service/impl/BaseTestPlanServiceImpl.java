package cn.master.backend.service.impl;

import cn.master.backend.constants.ModuleConstants;
import cn.master.backend.constants.TestPlanConstants;
import cn.master.backend.entity.TestPlan;
import cn.master.backend.entity.TestPlanModule;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.mapper.TestPlanMapper;
import cn.master.backend.payload.dto.plan.TestPlanResourceExecResultDTO;
import cn.master.backend.service.BaseTestPlanService;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
@Service("baseTestPlanService")
public class BaseTestPlanServiceImpl extends ServiceImpl<TestPlanMapper, TestPlan> implements BaseTestPlanService {
    @Override
    public void checkModule(String moduleId) {
        if (!StringUtils.equals(moduleId, ModuleConstants.DEFAULT_NODE_ID)) {
            QueryChain.of(TestPlanModule.class).where(TestPlanModule::getId).eq(moduleId)
                    .oneOpt()
                    .orElseThrow(() -> new MSException(Translator.get("module.not.exist")));
        }
    }

    @Override
    public Map<String, Map<String, List<String>>> parseExecResult(List<TestPlanResourceExecResultDTO> execResults) {
        Map<String, Map<String, List<String>>> returnMap = new HashMap<>();
        for (TestPlanResourceExecResultDTO execResult : execResults) {
            String groupId = execResult.getTestPlanGroupId();
            String planId = execResult.getTestPlanId();

            Map<String, List<String>> planMap;
            if (returnMap.containsKey(groupId)) {
                planMap = returnMap.get(groupId);
                List<String> resultList;
                if (planMap.containsKey(planId)) {
                    resultList = planMap.get(planId);
                } else {
                    resultList = new ArrayList<>();
                }
                resultList.add(execResult.getExecResult());
                planMap.put(planId, resultList);
            } else {
                planMap = new HashMap<>();
                List<String> resultList = new ArrayList<>();
                resultList.add(execResult.getExecResult());
                planMap.put(planId, resultList);
            }
            returnMap.put(groupId, planMap);
        }
        return returnMap;
    }

    @Override
    public String calculateTestPlanStatus(List<String> resultList) {
        List<String> calculateList = resultList.stream().distinct().toList();
        //目前只有三个状态。如果同时包含多种状态(进行中/未开始、进行中/已完成、已完成/未开始、进行中/未开始/已完成),根据算法可得测试计划都会是进行中
        if (calculateList.size() == 1) {
            if (calculateList.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
                return TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED;
            } else {
                return TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED;
            }
        } else {
            return TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY;
        }
    }

    @Override
    public String calculateStatusByChildren(List<String> childStatus) {
        // 首先去重
        childStatus = childStatus.stream().distinct().toList();

		/*
		1:全部都未开始 则为未开始
		2:全部都已完成 则为已完成
		3:包含进行中 则为进行中
		4:未开始+已完成：进行中
		 */
        if (childStatus.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY)) {
            return TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY;
        } else if (childStatus.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED) && childStatus.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED)) {
            return TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY;
        } else if (childStatus.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
            return TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED;
        } else {
            return TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED;
        }
    }
}
