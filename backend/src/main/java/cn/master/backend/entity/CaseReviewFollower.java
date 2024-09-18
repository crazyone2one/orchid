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
 * 用例评审和关注人的中间表 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用例评审和关注人的中间表")
@Table("case_review_follower")
public class CaseReviewFollower implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id
    @Schema(description = "id")
    private String id;

    /**
     * 评审ID
     */
    @Id
    @Schema(description = "评审ID")
    private String reviewId;

    /**
     * 关注人
     */
    @Id
    @Schema(description = "关注人")
    private String userId;

}
