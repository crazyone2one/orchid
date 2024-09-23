package cn.master.backend.entity;

import cn.master.backend.validation.Created;
import cn.master.backend.validation.Updated;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * 评审和评审人中间表 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评审和评审人中间表")
@Table("case_review_user")
public class CaseReviewUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Schema(description = "id")
    @Id
    private String id;

    @Schema(description =  "评审ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_user.review_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{case_review_user.review_id.length_range}", groups = {Created.class, Updated.class})
    private String reviewId;

    @Schema(description =  "评审人ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_user.user_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{case_review_user.user_id.length_range}", groups = {Created.class, Updated.class})
    private String userId;

}
