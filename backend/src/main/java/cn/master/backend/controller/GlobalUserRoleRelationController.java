package cn.master.backend.controller;

import cn.master.backend.payload.dto.system.request.GlobalUserRoleRelationQueryRequest;
import cn.master.backend.payload.dto.system.request.GlobalUserRoleRelationUpdateRequest;
import cn.master.backend.payload.dto.user.UserExcludeOptionDTO;
import cn.master.backend.payload.dto.user.UserRoleRelationUserDTO;
import cn.master.backend.service.GlobalUserRoleRelationService;
import cn.master.backend.util.SessionUtils;
import cn.master.backend.validation.Created;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户组关系 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-06
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "系统设置-系统-用户组-用户关联关系")
@RequestMapping("/user/role/relation/global")
public class GlobalUserRoleRelationController {

    private final GlobalUserRoleRelationService globalUserRoleRelationService;

    @PostMapping("/add")
    @PreAuthorize("hasPermission('SYSTEM_USER_ROLE','READ+UPDATE')")
    @Operation(summary = "系统设置-系统-用户组-用户关联关系-创建全局用户组和用户的关联关系")
    public void save(@Validated({Created.class}) @RequestBody GlobalUserRoleRelationUpdateRequest request) {
        request.setCreateUser(SessionUtils.getCurrentUserId());
        globalUserRoleRelationService.save(request);
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasPermission('SYSTEM_USER_ROLE','READ+UPDATE')")
    @Operation(summary = "系统设置-系统-用户组-用户关联关系-删除全局用户组和用户的关联关系")
    public void remove(@PathVariable String id) {
        globalUserRoleRelationService.remove(id);
    }

    @PostMapping("/list")
    @PreAuthorize("hasPermission('SYSTEM_USER_ROLE','READ')")
    @Operation(summary = "系统设置-系统-用户组-用户关联关系-获取全局用户组对应的用户列表")
    public Page<UserRoleRelationUserDTO> list(@Validated @RequestBody GlobalUserRoleRelationQueryRequest request) {
        return globalUserRoleRelationService.list(request);
    }

    @GetMapping("/user/option/{roleId}")
    @Operation(summary = "系统设置-系统-用户组-用户关联关系-获取需要关联的用户选项")
    @PreAuthorize("hasPermission('SYSTEM_USER_ROLE','READ')")
    public List<UserExcludeOptionDTO> getSelectOption(@Schema(description = "用户组ID", requiredMode = Schema.RequiredMode.REQUIRED)
                                                      @PathVariable String roleId,
                                                      @Schema(description = "查询关键字，根据邮箱和用户名查询", requiredMode = Schema.RequiredMode.REQUIRED)
                                                      @RequestParam(value = "keyword", required = false) String keyword) {
        return globalUserRoleRelationService.getExcludeSelectOption(roleId, keyword);
    }
}
