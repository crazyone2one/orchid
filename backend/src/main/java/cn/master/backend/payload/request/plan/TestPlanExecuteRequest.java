package cn.master.backend.payload.request.plan;

import cn.master.backend.constants.ApiBatchRunMode;
import cn.master.backend.constants.TaskTriggerMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
@Data
public class TestPlanExecuteRequest {
    @Schema(description = "执行ID")
    @NotBlank(message = "test_plan.not.exist")
    private String executeId;

    @Schema(description = "执行模式", allowableValues = {"SERIAL", "PARALLEL"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String runMode = ApiBatchRunMode.SERIAL.name();

    @Schema(description = "执行来源", allowableValues = {"MANUAL", "RUN", "SCHEDULE"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String executionSource = TaskTriggerMode.MANUAL.name();
}
