package cn.master.backend.payload.dto.user;

import cn.master.backend.entity.UserRole;
import cn.master.backend.entity.UserRolePermission;
import lombok.Data;

import java.util.List;

/**
 * @author Created by 11's papa on 08/06/2024
 **/
@Data
public class UserRoleResourceDTO {
    private List<UserRolePermission> permissions;
    private UserRole userRole;
    private List<UserRolePermission> userRolePermissions;
}
