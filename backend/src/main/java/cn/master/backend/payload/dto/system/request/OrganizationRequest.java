package cn.master.backend.payload.dto.system.request;

import cn.master.backend.payload.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 08/07/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class OrganizationRequest extends BasePageRequest {
    @Schema(description = "组织ID")
    private String organizationId;
}
