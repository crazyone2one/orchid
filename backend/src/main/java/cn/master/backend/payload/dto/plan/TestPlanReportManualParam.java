package cn.master.backend.payload.dto.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
@Data
@Builder
public class TestPlanReportManualParam {

    @Schema(description = "目标计划ID")
    private String targetId;

    @Schema(description = "手动生成的报告名称")
    private String manualName;
}
