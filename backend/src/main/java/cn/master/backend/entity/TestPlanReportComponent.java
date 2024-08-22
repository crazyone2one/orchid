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
 * 测试计划报告逐组件表 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试计划报告逐组件表")
@Table("test_plan_report_component")
public class TestPlanReportComponent implements Serializable {

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
     * 组件名称
     */
    @Schema(description = "组件名称")
    private String name;

    /**
     * 组件标题
     */
    @Schema(description = "组件标题")
    private String label;

    /**
     * 组件分类
     */
    @Schema(description = "组件分类")
    private String type;

    /**
     * 组件内容
     */
    @Schema(description = "组件内容")
    private String value;

    /**
     * 自定义排序，1开始整数递增
     */
    @Schema(description = "自定义排序，1开始整数递增")
    private Long pos;

}
