package cn.master.backend.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * 测试计划执行队列 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试计划执行队列")
@Table("test_plan_execution_queue")
public class TestPlanExecutionQueue implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 执行队列唯一ID
     */
    @Schema(description = "执行队列唯一ID")
    private String executeQueueId;

    /**
     * 测试计划id
     */
    @Schema(description = "测试计划id")
    private String testPlanId;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Long pos;

    /**
     * 预生成报告ID
     */
    @Schema(description = "预生成报告ID")
    private String prepareReportId;

    /**
     * 运行模式(SERIAL/PARALLEL)
     */
    @Schema(description = "运行模式(SERIAL/PARALLEL)")
    private String runMode;

    /**
     * 操作人
     */
    @Schema(description = "操作人")
    private String createUser;

    /**
     * 操作时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "操作时间")
    private LocalDateTime createTime;
    //队列ID
    @Column(ignore = true)
    private String queueId;
    // 队列类型
    @Column(ignore = true)
    private String queueType;
    @Column(ignore = true)
    private String parentQueueId;
    @Column(ignore = true)
    private String parentQueueType;
    @Column(ignore = true)
    private String sourceID;
    @Column(ignore = true)
    private String executionSource;
    @Column(ignore = true)
    private String testPlanCollectionJson;
    @Column(ignore = true)
    private boolean isLastOne = false;
    @Column(ignore = true)
    private boolean executeFinish = false;

    public TestPlanExecutionQueue(long pos, String createUser, LocalDateTime createTime, String queueId, String queueType, String parentQueueId, String parentQueueType, String sourceID, String runMode, String executionSource, String prepareReportId) {
        this.pos = pos;
        this.createUser = createUser;
        this.createTime = createTime;
        this.queueId = queueId;
        this.queueType = queueType;
        this.parentQueueId = parentQueueId;
        this.parentQueueType = parentQueueType;
        this.sourceID = sourceID;
        this.runMode = runMode;
        this.executionSource = executionSource;
        this.prepareReportId = prepareReportId;
    }
}
