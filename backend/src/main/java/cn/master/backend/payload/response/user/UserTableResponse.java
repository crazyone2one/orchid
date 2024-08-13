package cn.master.backend.payload.response.user;

import cn.master.backend.entity.Organization;
import cn.master.backend.entity.User;
import cn.master.backend.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by 11's papa on 08/13/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class UserTableResponse extends User {
    @Schema(description =  "用户所属组织")
    private List<Organization> organizationList = new ArrayList<>();
    @Schema(description =  "用户所属用户组")
    private List<UserRole> userRoleList = new ArrayList<>();

    public void setOrganization(Organization organization) {
        if (!organizationList.contains(organization)) {
            organizationList.add(organization);
        }
    }

    public void setUserRole(UserRole userRole) {
        if (!userRoleList.contains(userRole)) {
            userRoleList.add(userRole);
        }
    }
}
