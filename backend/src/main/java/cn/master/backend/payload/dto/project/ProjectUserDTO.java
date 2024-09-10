package cn.master.backend.payload.dto.project;

import cn.master.backend.entity.User;
import cn.master.backend.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Created by 11's papa on 09/09/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectUserDTO extends User {

    @Schema(description =  "用户组集合")
    private List<UserRole> userRoles;
}
