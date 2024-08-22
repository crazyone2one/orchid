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
 * 测试计划报告内容接口用例部分 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试计划报告内容接口用例部分")
@Table("test_plan_report_api_case")
public class TestPlanReportApiCase implements Serializable {

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
     * 环境ID
     */
    @Schema(description = "环境ID")
    private String environmentId;

    /**
     * 测试计划接口用例关联ID
     */
    @Schema(description = "测试计划接口用例关联ID")
    private String testPlanApiCaseId;

    /**
     * 接口用例ID
     */
    @Schema(description = "接口用例ID")
    private String apiCaseId;

    /**
     * 接口用例业务ID
     */
    @Schema(description = "接口用例业务ID")
    private Long apiCaseNum;

    /**
     * 接口用例名称
     */
    @Schema(description = "接口用例名称")
    private String apiCaseName;

    /**
     * 接口用例所属模块
     */
    @Schema(description = "接口用例所属模块")
    private String apiCaseModule;

    /**
     * 接口用例等级
     */
    @Schema(description = "接口用例等级")
    private String apiCasePriority;

    /**
     * 接口用例执行人
     */
    @Schema(description = "接口用例执行人")
    private String apiCaseExecuteUser;

    /**
     * 接口用例执行结果
     */
    @Schema(description = "接口用例执行结果")
    private String apiCaseExecuteResult;

    /**
     * 接口用例执行报告ID
     */
    @Schema(description = "接口用例执行报告ID")
    private String apiCaseExecuteReportId;

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
