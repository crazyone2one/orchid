package cn.master.backend.payload.request.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Created by 11's papa on 09/06/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class OrganizationMemberExtendRequest extends OrganizationMemberRequestByOrg {

    /**
     * 用户组ID集合
     */
    @Schema(description =  "用户组ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{userGroupId.id.not_null}")
    private List<String> userRoleIds;
}

