package cn.master.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * 测试计划报告内容统计 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试计划报告内容统计")
@Table("test_plan_report_summary")
public class TestPlanReportSummary implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 功能用例数量
     */
    @Schema(description = "功能用例数量")
    private Long functionalCaseCount;

    /**
     * 接口用例数量
     */
    @Schema(description = "接口用例数量")
    private Long apiCaseCount;

    /**
     * 场景用例数量
     */
    @Schema(description = "场景用例数量")
    private Long apiScenarioCount;

    /**
     * 缺陷数量
     */
    @Schema(description = "缺陷数量")
    private Long bugCount;

    /**
     * 测试计划报告ID
     */
    @Schema(description = "测试计划报告ID")
    private String testPlanReportId;

    /**
     * 总结
     */
    @Schema(description = "总结")
    private String summary;

    /**
     * 计划数量
     */
    @Schema(description = "计划数量")
    private Long planCount;

    /**
     * 功能用例执行结果
     */
    @Schema(description = "功能用例执行结果")
    private byte[] functionalExecuteResult;

    /**
     * 接口执行结果
     */
    @Schema(description = "接口执行结果")
    private byte[] apiExecuteResult;

    /**
     * 场景执行结果
     */
    @Schema(description = "场景执行结果")
    private byte[] scenarioExecuteResult;

    /**
     * 执行结果
     */
    @Schema(description = "执行结果")
    private byte[] executeResult;

}
