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
 * 测试计划报告内容缺陷部分 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试计划报告内容缺陷部分")
@Table("test_plan_report_bug")
public class TestPlanReportBug implements Serializable {

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
     * 缺陷ID
     */
    @Schema(description = "缺陷ID")
    private String bugId;

    /**
     * 缺陷业务ID
     */
    @Schema(description = "缺陷业务ID")
    private Long bugNum;

    /**
     * 缺陷标题
     */
    @Schema(description = "缺陷标题")
    private String bugTitle;

    /**
     * 缺陷状态
     */
    @Schema(description = "缺陷状态")
    private String bugStatus;

    /**
     * 缺陷处理人
     */
    @Schema(description = "缺陷处理人")
    private String bugHandleUser;

    /**
     * 缺陷用例数
     */
    @Schema(description = "缺陷用例数")
    private Long bugCaseCount;

}
