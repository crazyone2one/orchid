package cn.master.backend.handler.schedule;

import cn.master.backend.util.LogUtils;
import org.quartz.*;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
public abstract class BaseScheduleJob implements Job {
    protected String resourceId;

    protected String userId;

    protected String expression;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobKey jobKey = context.getTrigger().getJobKey();
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        this.resourceId = jobDataMap.getString("resourceId");
        this.userId = jobDataMap.getString("userId");
        this.expression = jobDataMap.getString("expression");

        LogUtils.info(jobKey.getGroup() + " Running: " + resourceId);
        businessExecute(context);
    }
    protected abstract void businessExecute(JobExecutionContext context);
}
