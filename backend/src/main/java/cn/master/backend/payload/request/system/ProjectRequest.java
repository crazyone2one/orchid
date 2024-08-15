package cn.master.backend.payload.request.system;

import cn.master.backend.payload.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 08/15/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectRequest extends BasePageRequest {
    @Schema(description =  "组织ID")
    private String organizationId;
    @Schema(description =  "项目ID")
    private String projectId;
}

