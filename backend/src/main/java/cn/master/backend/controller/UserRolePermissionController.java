package cn.master.backend.controller;

import com.mybatisflex.core.paginate.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import cn.master.backend.entity.UserRolePermission;
import cn.master.backend.service.BaseUserRolePermissionService;
import org.springframework.web.bind.annotation.RestController;
import java.io.Serializable;
import java.util.List;

/**
 * 用户组权限 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-06
 */
@RestController
@RequestMapping("/userRolePermission")
public class UserRolePermissionController {

    @Autowired
    private BaseUserRolePermissionService baseUserRolePermissionService;

    /**
     * 添加用户组权限。
     *
     * @param userRolePermission 用户组权限
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("save")
    public boolean save(@RequestBody UserRolePermission userRolePermission) {
        return baseUserRolePermissionService.save(userRolePermission);
    }

    /**
     * 根据主键删除用户组权限。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Serializable id) {
        return baseUserRolePermissionService.removeById(id);
    }

    /**
     * 根据主键更新用户组权限。
     *
     * @param userRolePermission 用户组权限
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody UserRolePermission userRolePermission) {
        return baseUserRolePermissionService.updateById(userRolePermission);
    }

    /**
     * 查询所有用户组权限。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<UserRolePermission> list() {
        return baseUserRolePermissionService.list();
    }

    /**
     * 根据用户组权限主键获取详细信息。
     *
     * @param id 用户组权限主键
     * @return 用户组权限详情
     */
    @GetMapping("getInfo/{id}")
    public UserRolePermission getInfo(@PathVariable Serializable id) {
        return baseUserRolePermissionService.getById(id);
    }

    /**
     * 分页查询用户组权限。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<UserRolePermission> page(Page<UserRolePermission> page) {
        return baseUserRolePermissionService.page(page);
    }

}
