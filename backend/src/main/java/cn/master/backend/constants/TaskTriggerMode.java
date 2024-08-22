package cn.master.backend.constants;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
public enum TaskTriggerMode {
    /**
     * 定时任务
     */
    SCHEDULE,
    /**
     * 手动执行
     */
    MANUAL,
    /**
     * 接口调用
     */
    API,
    /**
     * 批量执行
     */
    BATCH
}
