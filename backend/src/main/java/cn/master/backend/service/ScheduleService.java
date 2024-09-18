package cn.master.backend.service;

import cn.master.backend.handler.job.TestPlanScheduleJob;
import cn.master.backend.payload.dto.system.ScheduleConfig;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.Schedule;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.util.List;

/**
 * 定时任务 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-16
 */
public interface ScheduleService extends IService<Schedule> {

    void deleteByResourceId(String testPlanId, JobKey jobKey, TriggerKey triggerKey);

    void deleteByResourceIds(List<String> resourceIds, String group);

    Schedule getScheduleByResource(String resourceId, String job);

    void updateIfExist(String resourceId, boolean enable, JobKey jobKey, TriggerKey triggerKey, Class clazz, String operator);

    String scheduleConfig(ScheduleConfig scheduleConfig, JobKey jobKey, TriggerKey triggerKey, Class clazz, String operator);

    void deleteByProjectId(String projectId);
}
