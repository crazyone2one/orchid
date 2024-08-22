package cn.master.backend.handler.job;

import cn.master.backend.entity.Schedule;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.util.LogUtils;
import jakarta.annotation.Resource;
import org.quartz.*;
import org.springframework.stereotype.Component;

/**
 * @author Created by 11's papa on 08/20/2024
 **/
@Component
public class ScheduleManager {
    @Resource
    private Scheduler scheduler;

    public void removeJob(JobKey jobKey, TriggerKey triggerKey) {
        try {
            LogUtils.info("RemoveJob: " + jobKey.getName() + "," + jobKey.getGroup());
            scheduler.pauseTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);
            scheduler.deleteJob(jobKey);
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void addCronJob(JobKey jobKey, TriggerKey triggerKey, Class<? extends Job> jobClass, String cron, JobDataMap jobDataMap) {
        try {
            LogUtils.info("addCronJob: " + triggerKey.getName() + "," + triggerKey.getGroup());
            JobBuilder jobBuilder = JobBuilder.newJob(jobClass).withIdentity(jobKey);
            if (jobDataMap != null) {
                jobBuilder.usingJobData(jobDataMap);
            }

            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
            triggerBuilder.withIdentity(triggerKey);
            triggerBuilder.startNow();
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
            CronTrigger trigger = (CronTrigger) triggerBuilder.build();
            scheduler.scheduleJob(jobBuilder.build(), trigger);

        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException("定时任务配置异常: " + e.getMessage());
        }
    }

    public JobDataMap getDefaultJobDataMap(Schedule schedule, String expression, String userId) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("resourceId", schedule.getResourceId());
        jobDataMap.put("expression", expression);
        jobDataMap.put("userId", userId);
        jobDataMap.put("config", schedule.getConfig());
        jobDataMap.put("projectId", schedule.getProjectId());
        return jobDataMap;
    }
}
