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
 * 功能用例评论 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "功能用例评论")
@Table("functional_case_comment")
public class FunctionalCaseComment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 功能用例ID
     */
    @Schema(description = "功能用例ID")
    private String caseId;

    /**
     * 评论人
     */
    @Schema(description = "评论人")
    private String createUser;

    /**
     * 父评论ID
     */
    @Schema(description = "父评论ID")
    private String parentId;

    /**
     * 资源ID: 评审ID/测试计划ID
     */
    @Schema(description = "资源ID: 评审ID/测试计划ID")
    private String resourceId;

    /**
     * 通知人
     */
    @Schema(description = "通知人")
    private String notifier;

    /**
     * 内容
     */
    @Schema(description = "内容")
    private String content;

    /**
     * 回复人
     */
    @Schema(description = "回复人")
    private String replyUser;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
