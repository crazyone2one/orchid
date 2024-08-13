package cn.master.backend.payload.dto.system;

import cn.master.backend.entity.Organization;
import cn.master.backend.entity.User;
import cn.master.backend.validation.Created;
import cn.master.backend.validation.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class OrganizationDTO extends Organization {
    @Schema(description =  "成员数量")
    private Integer memberCount;

    /**
     * 项目数量
     */
    @Schema(description =  "项目数量" )
    private Integer projectCount;

    /**
     * 列表组织管理员集合
     */
    @Schema(description =  "列表组织管理员集合")
    private List<User> orgAdmins;

    /**
     * 组织管理员ID集合(新增, 编辑), 必填
     */
    @Schema(description =  "组织管理员ID集合")
    @NotEmpty(groups = {Created.class, Updated.class}, message = "{member.id.not_empty}")
    private List<String> userIds;

    /**
     * 创建人是否是管理员
     */
    @Schema(description =  "创建人是否是管理员")
    private Boolean orgCreateUserIsAdmin;

    /**
     * 剩余删除保留天数
     */
    @Schema(description =  "剩余删除保留天数")
    private Integer remainDayCount;
}
