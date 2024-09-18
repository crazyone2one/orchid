package cn.master.backend.payload.request.functional;

import cn.master.backend.payload.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Created by 11's papa on 09/13/2024
 **/
@Getter
@Setter
public class QueryDemandListRequest extends BasePageRequest {
    @Schema(description = "功能用例id")
    @NotBlank(message = "{demand_request.case_id.not_blank}")
    private String caseId;

    @Schema(description = "当前项目id")
    @NotBlank(message = "{demand_request.project_id.not_blank}")
    private String projectId;
}

