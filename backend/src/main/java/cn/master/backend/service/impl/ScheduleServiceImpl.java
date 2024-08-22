package cn.master.backend.service.impl;

import cn.master.backend.entity.Schedule;
import cn.master.backend.handler.job.ScheduleManager;
import cn.master.backend.handler.uid.IDGenerator;
import cn.master.backend.mapper.ScheduleMapper;
import cn.master.backend.payload.dto.system.ScheduleConfig;
import cn.master.backend.service.ScheduleService;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 定时任务 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-16
 */
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {
    private final ScheduleManager scheduleManager;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteByResourceId(String testPlanId, JobKey jobKey, TriggerKey triggerKey) {
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain().where(Schedule::getResourceId).eq(testPlanId)));
        scheduleManager.removeJob(jobKey, triggerKey);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteByResourceIds(List<String> resourceIds, String group) {
        QueryChain<Schedule> queryChain = queryChain().where(Schedule::getResourceId).in(resourceIds);
        for (String resourceId : resourceIds) {
            removeJob(resourceId, group);
        }
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain));
    }

    @Override
    public Schedule getScheduleByResource(String resourceId, String job) {
        List<Schedule> schedules = queryChain().where(Schedule::getResourceId).eq(resourceId)
                .and(Schedule::getJob).eq(job).list();
        if (CollectionUtils.isNotEmpty(schedules)) {
            return schedules.getFirst();
        }
        return null;
    }

    @Override
    public void updateIfExist(String resourceId, boolean enable, JobKey jobKey, TriggerKey triggerKey, Class clazz, String operator) {
        List<Schedule> schedules = queryChain().where(Schedule::getResourceId).eq(resourceId)
                .and(Schedule::getJob).eq(clazz.getName()).list();
        if (CollectionUtils.isNotEmpty(schedules)) {
            Schedule schedule = schedules.getFirst();
            if (!schedule.getEnable().equals(enable)) {
                updateChain().set(Schedule::getEnable, enable).where(Schedule::getId).eq(schedule.getId()).update();
                // todo apiScheduleNoticeService.sendScheduleNotice(schedule, operator);
                if (enable) {
                    scheduleManager.addCronJob(jobKey, triggerKey, clazz, schedule.getValue(),
                            scheduleManager.getDefaultJobDataMap(schedule, schedule.getValue(), schedule.getCreateUser()));
                } else {
                    scheduleManager.removeJob(jobKey, triggerKey);
                }
            }
        }
    }

    @Override
    public String scheduleConfig(ScheduleConfig scheduleConfig, JobKey jobKey, TriggerKey triggerKey, Class clazz, String operator) {
        Schedule schedule;
        QueryChain<Schedule> scheduleQueryChain = queryChain().where(Schedule::getResourceId).eq(scheduleConfig.getResourceId())
                .and(Schedule::getJob).eq(clazz.getName());
        List<Schedule> scheduleList = scheduleQueryChain.list();
        boolean needSendNotice = false;
        if (CollectionUtils.isNotEmpty(scheduleList)) {
            needSendNotice = !scheduleList.getFirst().getEnable().equals(scheduleConfig.getEnable());
            schedule = scheduleConfig.genCronSchedule(scheduleList.getFirst());
            schedule.setJob(clazz.getName());
            mapper.updateByQuery(schedule, scheduleQueryChain);
        } else {
            schedule = scheduleConfig.genCronSchedule(null);
            schedule.setJob(clazz.getName());
            schedule.setCreateUser(operator);
            mapper.insert(schedule);
        }
        //通知
        if ((CollectionUtils.isEmpty(scheduleList) && BooleanUtils.isTrue(scheduleConfig.getEnable()))
                || needSendNotice) {
            // todo apiScheduleNoticeService.sendScheduleNotice(schedule, operator);
        }


        JobDataMap jobDataMap = scheduleManager.getDefaultJobDataMap(schedule, scheduleConfig.getCron(), schedule.getCreateUser());

        /*
        scheduleManager.modifyCronJobTime方法如同它的方法名所说，只能修改定时任务的触发时间。
        如果定时任务的配置数据jobData发生了变化，上面方法是无法更新配置数据的。
        所以，如果配置数据发生了变化，做法就是先删除运行中的定时任务，再重新添加定时任务。

        以上的更新逻辑配合 enable 开关，可以简化为下列写法：
         */
        scheduleManager.removeJob(jobKey, triggerKey);
        if (BooleanUtils.isTrue(schedule.getEnable())) {
            scheduleManager.addCronJob(jobKey, triggerKey, clazz, scheduleConfig.getCron(), jobDataMap);
        }
        return schedule.getId();
    }

    private void removeJob(String key, String job) {
        scheduleManager.removeJob(new JobKey(key, job), new TriggerKey(key, job));
    }
}
