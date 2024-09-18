package cn.master.backend.entity;

import cn.master.backend.validation.Created;
import cn.master.backend.validation.Updated;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.mybatisflex.core.handler.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;
import java.util.List;

/**
 * 用例评审 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用例评审")
@Table("case_review")
public class CaseReview implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{case_review.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    /**
     * 业务ID
     */
    @Schema(description = "业务ID")
    private Long num;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{case_review.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "模块id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.module_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{case_review.module_id.length_range}", groups = {Created.class, Updated.class})
    private String moduleId;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{case_review.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "评审状态：未开始/进行中/已完成/已结束/已归档", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{case_review.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description = "通过标准：单人通过/全部通过", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.review_pass_rule.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{case_review.review_pass_rule.length_range}", groups = {Created.class, Updated.class})
    private String reviewPassRule;

    @Schema(description = "自定义排序，间隔5000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{case_review.pos.not_blank}", groups = {Created.class})
    private Long pos;

    /**
     * 评审开始时间
     */
    @Schema(description = "评审开始时间")
    private LocalDateTime startTime;

    /**
     * 评审结束时间
     */
    @Schema(description = "评审结束时间")
    private LocalDateTime endTime;

    @Schema(description = "用例数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{case_review.case_count.not_blank}", groups = {Created.class})
    private Integer caseCount;

    @Schema(description = "通过率(保留两位小数)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{case_review.pass_rate.not_blank}", groups = {Created.class})
    private BigDecimal passRate;

    /**
     * 标签
     */
    @Schema(description = "标签")
    @Column(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

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
     * 更新时间
     */
    @Schema(description = "更新时间")
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    private LocalDateTime updateTime;

    /**
     * 更新人
     */
    @Schema(description = "更新人")
    private String updateUser;

}
