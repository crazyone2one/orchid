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
 * 测试计划报告附件关系表 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试计划报告附件关系表")
@Table("test_plan_report_attachment")
public class TestPlanReportAttachment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 报告ID
     */
    @Schema(description = "报告ID")
    private String testPlanReportId;

    /**
     * 文件ID
     */
    @Schema(description = "文件ID")
    private String fileId;

    /**
     * 文件名称
     */
    @Schema(description = "文件名称")
    private String fileName;

    /**
     * 文件大小
     */
    @Schema(description = "文件大小")
    private Long size;

    /**
     * 文件来源
     */
    @Schema(description = "文件来源")
    private String source;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createUser;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
