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
 * 测试计划关联场景用例 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试计划关联场景用例")
@Table("test_plan_api_scenario")
public class TestPlanApiScenario implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 测试计划ID
     */
    @Schema(description = "测试计划ID")
    private String testPlanId;

    /**
     * 场景ID
     */
    @Schema(description = "场景ID")
    private String apiScenarioId;

    /**
     * 所属环境或环境组id
     */
    @Schema(description = "所属环境或环境组id")
    private String environmentId;

    /**
     * 执行人
     */
    @Schema(description = "执行人")
    private String executeUser;

    /**
     * 最后执行结果
     */
    @Schema(description = "最后执行结果")
    private String lastExecResult;

    /**
     * 最后执行报告
     */
    @Schema(description = "最后执行报告")
    private String lastExecReportId;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createUser;

    /**
     * 自定义排序，间隔5000
     */
    @Schema(description = "自定义排序，间隔5000")
    private Long pos;

    /**
     * 测试计划集id
     */
    @Schema(description = "测试计划集id")
    private String testPlanCollectionId;

    /**
     * 是否为环境组
     */
    @Schema(description = "是否为环境组")
    private Boolean grouped;

    /**
     * 最后执行时间
     */
    @Schema(description = "最后执行时间")
    private Long lastExecTime;

}
