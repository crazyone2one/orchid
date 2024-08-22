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
 * 测试计划报告内容功能用例部分 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试计划报告内容功能用例部分")
@Table("test_plan_report_function_case")
public class TestPlanReportFunctionCase implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 测试计划报告ID
     */
    @Schema(description = "测试计划报告ID")
    private String testPlanReportId;

    /**
     * 测试计划功能用例关联ID(同一计划下可重复关联, 暂时保留)
     */
    @Schema(description = "测试计划功能用例关联ID(同一计划下可重复关联, 暂时保留)")
    private String testPlanFunctionCaseId;

    /**
     * 功能用例ID
     */
    @Schema(description = "功能用例ID")
    private String functionCaseId;

    /**
     * 功能用例业务ID
     */
    @Schema(description = "功能用例业务ID")
    private Long functionCaseNum;

    /**
     * 功能用例名称
     */
    @Schema(description = "功能用例名称")
    private String functionCaseName;

    /**
     * 功能用例所属模块
     */
    @Schema(description = "功能用例所属模块")
    private String functionCaseModule;

    /**
     * 功能用例用例等级
     */
    @Schema(description = "功能用例用例等级")
    private String functionCasePriority;

    /**
     * 功能用例执行人
     */
    @Schema(description = "功能用例执行人")
    private String functionCaseExecuteUser;

    /**
     * 功能用例关联缺陷数
     */
    @Schema(description = "功能用例关联缺陷数")
    private Long functionCaseBugCount;

    /**
     * 执行结果
     */
    @Schema(description = "执行结果")
    private String functionCaseExecuteResult;

    /**
     * 测试集ID
     */
    @Schema(description = "测试集ID")
    private String testPlanCollectionId;

    /**
     * 自定义排序
     */
    @Schema(description = "自定义排序")
    private Long pos;

    /**
     * 执行报告ID
     */
    @Schema(description = "执行报告ID")
    private String functionCaseExecuteReportId;

    /**
     * 测试计划名称
     */
    @Schema(description = "测试计划名称")
    private String testPlanName;

}
