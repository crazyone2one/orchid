package cn.master.backend.service;

import cn.master.backend.constants.ScheduleResourceType;
import cn.master.backend.constants.TestPlanConstants;
import cn.master.backend.entity.TestPlan;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.handler.job.TestPlanScheduleJob;
import cn.master.backend.payload.dto.system.ScheduleConfig;
import cn.master.backend.payload.request.system.BaseScheduleConfigRequest;
import cn.master.backend.util.JSON;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.query.QueryChain;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Created by 11's papa on 08/22/2024
 **/
@Service
@RequiredArgsConstructor
public class TestPlanScheduleService {
    private final ScheduleService scheduleService;

    public String scheduleConfig(BaseScheduleConfigRequest request, String operator) {
        TestPlan testPlan = QueryChain.of(TestPlan.class)
                .where(TestPlan::getId).eq(request.getResourceId())
                .oneOpt()
                .orElseThrow(() -> new MSException(Translator.get("test_plan.not.exist")));
        ScheduleConfig scheduleConfig = ScheduleConfig.builder()
                .resourceId(testPlan.getId())
                .key(testPlan.getId())
                .projectId(testPlan.getProjectId())
                .name(testPlan.getName())
                .enable(request.isEnable())
                .cron(request.getCron())
                .resourceType(ScheduleResourceType.TEST_PLAN.name())
                .config(JSON.toJSONString(request.getRunConfig()))
                .build();
        if (request.isEnable() && StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            //配置开启的测试计划组定时任务，要将组下的所有测试计划定时任务都关闭掉
            List<TestPlan> children = QueryChain.of(TestPlan.class).where(TestPlan::getGroupId).eq(testPlan.getId())
                    .and(TestPlan::getStatus).ne(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)
                    .orderBy("pos", true).list();
            for (TestPlan child : children) {
                scheduleService.updateIfExist(child.getId(), false, TestPlanScheduleJob.getJobKey(testPlan.getId()),
                        TestPlanScheduleJob.getTriggerKey(testPlan.getId()),
                        TestPlanScheduleJob.class, operator);
            }
        }

        return scheduleService.scheduleConfig(
                scheduleConfig,
                TestPlanScheduleJob.getJobKey(testPlan.getId()),
                TestPlanScheduleJob.getTriggerKey(testPlan.getId()),
                TestPlanScheduleJob.class,
                operator);
    }
}
