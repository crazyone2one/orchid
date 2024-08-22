package cn.master.backend.constants;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
public enum ExecStatus {
    /**
     * 未执行
     */
    PENDING,
    /**
     * 执行中
     */
    RUNNING,
    /**
     * 重新执行中
     */
    RERUNNING,
    /**
     * 已停止
     */
    STOPPED,
    /**
     * 完成
     */
    COMPLETED
}
