package cn.master.backend.payload.dto.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
@Data
public class TestPlanReportPostParam {

    @Schema(description = "报告ID")
    private String reportId;

    @Schema(description = "计划开始执行时间")
    private LocalDateTime executeTime;

    @Schema(description = "计划结束时间")
    private LocalDateTime endTime;

    @Schema(description = "执行状态")
    private String execStatus;
}
