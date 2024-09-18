package cn.master.backend.payload.request.functional;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 09/12/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class CaseReviewCopyRequest extends CaseReviewRequest{

    @Schema(description = "被复制的用例评审id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.copy_id.not_blank}")
    private String copyId;
}

