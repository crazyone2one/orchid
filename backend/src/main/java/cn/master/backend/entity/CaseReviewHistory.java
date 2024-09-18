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
 * 评审历史表 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评审历史表")
@Table("case_review_history")
public class CaseReviewHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 评审ID
     */
    @Schema(description = "评审ID")
    private String reviewId;

    /**
     * 用例ID
     */
    @Schema(description = "用例ID")
    private String caseId;

    /**
     * 评审意见
     */
    @Schema(description = "评审意见")
    private byte[] content;

    /**
     * 评审结果：通过/不通过/建议
     */
    @Schema(description = "评审结果：通过/不通过/建议")
    private String status;

    /**
     * 是否是取消关联或评审被删除的：0-否，1-是
     */
    @Schema(description = "是否是取消关联或评审被删除的：0-否，1-是")
    private Boolean deleted;

    /**
     * 是否是废弃的评审记录：0-否，1-是
     */
    @Schema(description = "是否是废弃的评审记录：0-否，1-是")
    private Boolean abandoned;

    /**
     * 通知人
     */
    @Schema(description = "通知人")
    private String notifier;

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

}
