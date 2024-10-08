package cn.master.backend.payload.request.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Created by 11's papa on 08/20/2024
 **/
@Data
public class TestPlanFollowerRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_follower.user_id.not_blank}")
    private String userId;

    @Schema(description = "用例id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.id.not_blank}")
    private String testPlanId;
}
