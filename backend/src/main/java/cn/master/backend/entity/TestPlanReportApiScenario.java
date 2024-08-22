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
 * 测试计划报告内容接口场景部分 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试计划报告内容接口场景部分")
@Table("test_plan_report_api_scenario")
public class TestPlanReportApiScenario implements Serializable {

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
     * 测试集ID
     */
    @Schema(description = "测试集ID")
    private String testPlanCollectionId;

    /**
     * 是否环境组
     */
    @Schema(description = "是否环境组")
    private Boolean grouped;

    /**
     * 环境ID
     */
    @Schema(description = "环境ID")
    private String environmentId;

    /**
     * 测试计划场景用例关联ID
     */
    @Schema(description = "测试计划场景用例关联ID")
    private String testPlanApiScenarioId;

    /**
     * 场景用例ID
     */
    @Schema(description = "场景用例ID")
    private String apiScenarioId;

    /**
     * 场景用例业务ID
     */
    @Schema(description = "场景用例业务ID")
    private Long apiScenarioNum;

    /**
     * 场景用例名称
     */
    @Schema(description = "场景用例名称")
    private String apiScenarioName;

    /**
     * 场景用例所属模块
     */
    @Schema(description = "场景用例所属模块")
    private String apiScenarioModule;

    /**
     * 场景用例等级
     */
    @Schema(description = "场景用例等级")
    private String apiScenarioPriority;

    /**
     * 场景用例执行人
     */
    @Schema(description = "场景用例执行人")
    private String apiScenarioExecuteUser;

    /**
     * 场景用例执行结果
     */
    @Schema(description = "场景用例执行结果")
    private String apiScenarioExecuteResult;

    /**
     * 场景用例执行报告ID
     */
    @Schema(description = "场景用例执行报告ID")
    private String apiScenarioExecuteReportId;

    /**
     * 自定义排序
     */
    @Schema(description = "自定义排序")
    private Long pos;

    /**
     * 测试计划名称
     */
    @Schema(description = "测试计划名称")
    private String testPlanName;

}
