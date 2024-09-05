package cn.master.backend.payload.request.project;

import cn.master.backend.payload.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 09/05/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectUserRoleRequest extends BasePageRequest {

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private String projectId;
}
