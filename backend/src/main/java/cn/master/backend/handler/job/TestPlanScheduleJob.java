package cn.master.backend.handler.job;

import cn.master.backend.constants.ApiBatchRunMode;
import cn.master.backend.constants.ApiExecuteRunMode;
import cn.master.backend.handler.schedule.BaseScheduleJob;
import cn.master.backend.handler.uid.IDGenerator;
import cn.master.backend.payload.request.plan.TestPlanExecuteRequest;
import cn.master.backend.service.TestPlanExecuteService;
import cn.master.backend.util.CommonBeanFactory;
import cn.master.backend.util.JSON;
import cn.master.backend.util.LogUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.util.Map;

/**
 * @author Created by 11's papa on 08/16/2024
 **/

public class TestPlanScheduleJob extends BaseScheduleJob {

    @Override
    protected void businessExecute(JobExecutionContext context) {
        TestPlanExecuteService testPlanExecuteService = CommonBeanFactory.getBean(TestPlanExecuteService.class);
        Map<String, String> runConfig = JSON.parseObject(context.getJobDetail().getJobDataMap().get("config").toString(), Map.class);
        String runMode = runConfig.containsKey("runMode") ? runConfig.get("runMode") : ApiBatchRunMode.SERIAL.name();
        LogUtils.info("开始执行测试计划的定时任务. ID：" + resourceId);
        Thread.startVirtualThread(() ->
                {
                    assert testPlanExecuteService != null;
                    testPlanExecuteService.singleExecuteTestPlan(new TestPlanExecuteRequest() {{
                        this.setExecuteId(resourceId);
                        this.setRunMode(runMode);
                        this.setExecutionSource(ApiExecuteRunMode.SCHEDULE.name());
                    }}, IDGenerator.nextStr(), userId);
                }
        );
    }

    public static JobKey getJobKey(String testPlanId) {
        return new JobKey(testPlanId, TestPlanScheduleJob.class.getName());
    }

    public static TriggerKey getTriggerKey(String testPlanId) {
        return new TriggerKey(testPlanId, TestPlanScheduleJob.class.getName());
    }
}
