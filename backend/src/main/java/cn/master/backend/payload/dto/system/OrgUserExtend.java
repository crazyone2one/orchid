package cn.master.backend.payload.dto.system;

import cn.master.backend.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Created by 11's papa on 09/06/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class OrgUserExtend extends User {

    @Schema(description =  "项目ID名称集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OptionDTO> projectIdNameMap;;

    @Schema(description =  "用户组ID名称集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OptionDTO> userRoleIdNameMap;
}
