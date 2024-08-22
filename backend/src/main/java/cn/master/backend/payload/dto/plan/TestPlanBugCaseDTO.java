package cn.master.backend.payload.dto.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Created by 11's papa on 08/20/2024
 **/
@Data
public class TestPlanBugCaseDTO {
    @Schema(description = "用例ID")
    private String id;
    @Schema(description = "用例业务ID")
    private String num;
    @Schema(description = "缺陷ID")
    private String bugId;
    @Schema(description = "用例名称")
    private String name;
}
