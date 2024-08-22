package cn.master.backend.payload.request.plan;

import cn.master.backend.payload.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 08/22/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanExecuteHisPageRequest extends BasePageRequest {

    @Schema(description = "测试计划/计划组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String testPlanId;
}
