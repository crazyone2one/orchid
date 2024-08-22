package cn.master.backend.constants;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
public enum ApiExecuteRunMode {
    /**
     * 手动运行
     */
    RUN,
    /**
     * 前端调试
     */
    FRONTEND_DEBUG,
    /**
     * 后端调试
     */
    BACKEND_DEBUG,

    /**
     * jenkins 触发
     */
    JENKINS,
    /**
     * 定时任务
     */
    SCHEDULE
}
