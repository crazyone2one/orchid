package cn.master.backend.payload.request.functional;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Created by 11's papa on 09/12/2024
 **/
@Data
public class CaseReviewFollowerRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug_follower.user_id.not_blank}")
    private String userId;

    @Schema(description = "用例评审id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_demand.case_id.not_blank}")
    private String caseReviewId;

}

