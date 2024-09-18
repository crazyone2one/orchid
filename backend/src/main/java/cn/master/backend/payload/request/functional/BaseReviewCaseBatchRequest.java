package cn.master.backend.payload.request.functional;

import cn.master.backend.payload.dto.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Created by 11's papa on 09/12/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class BaseReviewCaseBatchRequest extends TableBatchProcessDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "评审id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_user.review_id.not_blank}")
    private String reviewId;

    @Schema(description = "模块id")
    private List<String> moduleIds;
}

