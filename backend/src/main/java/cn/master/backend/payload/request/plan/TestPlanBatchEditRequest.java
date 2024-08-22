package cn.master.backend.payload.request.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Created by 11's papa on 08/15/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class TestPlanBatchEditRequest extends TestPlanBatchProcessRequest {

    @Schema(description = "是否追加")
    private boolean append = true;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "定时任务是否开启")
    private boolean scheduleOpen;

    @Schema(description = "本次编辑的字段", allowableValues = {"TAGS", "SCHEDULE"})
    private String editColumn;
}
