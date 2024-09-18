package cn.master.backend.payload.request.functional;

import cn.master.backend.payload.request.system.PosRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 09/13/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class CaseReviewFunctionalCasePosRequest extends PosRequest {

    @Schema(description = "用例评审Id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.case_review_id.not_blank}")
    private String reviewId;
}
