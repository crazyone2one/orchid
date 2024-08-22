package cn.master.backend.payload.response.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Created by 11's papa on 08/20/2024
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestPlanSingleOperationResponse {
    @Schema(description = "处理成功的ID")
    private String operationId;
}
