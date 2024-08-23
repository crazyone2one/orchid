package cn.master.backend.service.impl;

import cn.master.backend.constants.UserRoleType;
import cn.master.backend.entity.*;
import cn.master.backend.mapper.UserMapper;
import cn.master.backend.payload.dto.user.UserDTO;
import cn.master.backend.payload.dto.user.UserRolePermissionDTO;
import cn.master.backend.payload.dto.user.UserRoleResourceDTO;
import cn.master.backend.payload.request.AuthenticationRequest;
import cn.master.backend.security.CustomUserDetails;
import cn.master.backend.security.JwtGenerator;
import cn.master.backend.security.UserDetailsServiceImpl;
import cn.master.backend.service.AuthenticationService;
import cn.master.backend.service.UserKeyService;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.OrganizationTableDef.ORGANIZATION;
import static cn.master.backend.entity.table.ProjectTableDef.PROJECT;
import static cn.master.backend.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;

/**
 * @author Created by 11's papa on 08/06/2024
 **/
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtGenerator jwtGenerator;
    private final UserKeyService userKeyService;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserMapper userMapper;

    @Override
    public UserDTO authenticate(AuthenticationRequest request) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        String accessToken = jwtGenerator.generateToken(request.getUsername(), userDetailsService.loadUserByUsername(request.getUsername()).getAuthorities());
        UserKey userKey = userKeyService.createRefreshToken(request.getUsername(), accessToken);
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        UserDTO response = getUserDTO(principal.getId());
        autoSwitch(response);
        response = getUserDTO(principal.getId());
        response.setAccessToken(accessToken);
        response.setRefreshToken(userKey.getRefreshToken());
        return response;
    }

    private UserRolePermissionDTO getUserRolePermission(String userId) {
        UserRolePermissionDTO permissionDTO = new UserRolePermissionDTO();
        List<UserRoleResourceDTO> list = new ArrayList<>();
        List<UserRoleRelation> userRoleRelations = QueryChain.of(UserRoleRelation.class).where(UserRoleRelation::getUserId).eq(userId).list();
        if (userRoleRelations.isEmpty()) {
            return permissionDTO;
        }
        permissionDTO.setUserRoleRelations(userRoleRelations);
        List<String> roleList = userRoleRelations.stream().map(UserRoleRelation::getRoleId).collect(Collectors.toList());
        List<UserRole> userRoles = QueryChain.of(UserRole.class).where(UserRole::getId).in(roleList).list();
        permissionDTO.setUserRoles(userRoles);
        for (UserRole gp : userRoles) {
            UserRoleResourceDTO dto = new UserRoleResourceDTO();
            dto.setUserRole(gp);
            List<UserRolePermission> userRolePermissions = QueryChain.of(UserRolePermission.class).where(UserRolePermission::getRoleId).eq(gp.getId()).list();
            dto.setUserRolePermissions(userRolePermissions);
            list.add(dto);
        }
        permissionDTO.setList(list);
        return permissionDTO;
    }

    private void autoSwitch(UserDTO user) {
// 判断是否是系统管理员
        if (isSystemAdmin(user)) {
            return;
        }
        // 用户有 last_project_id 权限
        if (hasLastProjectPermission(user)) {
            return;
        }
        // 用户有 last_organization_id 权限
        if (hasLastOrganizationPermission(user)) {
            return;
        }
        // 判断其他权限
        checkNewOrganizationAndProject(user);
    }

    private void checkNewOrganizationAndProject(UserDTO user) {
        List<UserRoleRelation> userRoleRelations = user.getUserRoleRelations();
        List<String> projectRoleIds = user.getUserRoles()
                .stream().filter(ug -> StringUtils.equals(ug.getType(), UserRoleType.PROJECT.name()))
                .map(UserRole::getId)
                .toList();
        List<UserRoleRelation> project = userRoleRelations.stream().filter(ug -> projectRoleIds.contains(ug.getRoleId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(project)) {
            List<String> organizationIds = user.getUserRoles()
                    .stream()
                    .filter(ug -> StringUtils.equals(ug.getType(), UserRoleType.ORGANIZATION.name()))
                    .map(UserRole::getId)
                    .toList();
            List<UserRoleRelation> organizations = userRoleRelations.stream().filter(ug -> organizationIds.contains(ug.getRoleId()))
                    .toList();
            if (CollectionUtils.isNotEmpty(organizations)) {
                //获取所有的组织
                List<String> orgIds = organizations.stream().map(UserRoleRelation::getSourceId).collect(Collectors.toList());
                List<Organization> organizationsList = QueryChain.of(Organization.class)
                        .where(ORGANIZATION.ID.in(orgIds).and(ORGANIZATION.ENABLE.eq(true)))
                        .list();
                if (CollectionUtils.isNotEmpty(organizationsList)) {
                    String wsId = organizationsList.getFirst().getId();
                    switchUserResource(wsId, user);
                }
            } else {
                // 用户登录之后没有项目和组织的权限就把值清空
                user.setLastOrganizationId(StringUtils.EMPTY);
                user.setLastProjectId(StringUtils.EMPTY);
                UpdateChain.of(User.class).set(User::getLastOrganizationId, StringUtils.EMPTY)
                        .set(User::getLastProjectId, StringUtils.EMPTY)
                        .where(User::getId).eq(user.getId()).update();
            }
        } else {
            UserRoleRelation userRoleRelation = project.stream().filter(p -> StringUtils.isNotBlank(p.getSourceId()))
                    .toList().getFirst();
            String projectId = userRoleRelation.getSourceId();
            Project p = QueryChain.of(Project.class).where(PROJECT.ID.eq(projectId)).one();
            String wsId = p.getOrganizationId();
            UpdateChain.of(User.class).set(User::getLastProjectId, projectId)
                    .set(User::getLastOrganizationId, wsId)
                    .where(User::getId).eq(user.getId()).update();
        }
    }

    private void switchUserResource(String sourceId, UserDTO sessionUser) {
        // 获取最新UserDTO
        UserDTO user = getUserDTO(sessionUser.getId());
        User newUser = new User();
        user.setLastOrganizationId(sourceId);
        sessionUser.setLastOrganizationId(sourceId);
        user.setLastProjectId(StringUtils.EMPTY);
        List<Project> projects = getProjectListByWsAndUserId(sessionUser.getId(), sourceId);
        if (CollectionUtils.isNotEmpty(projects)) {
            user.setLastProjectId(projects.getFirst().getId());
        }
        BeanUtils.copyProperties(user, newUser);
        // 切换组织或组织之后更新 session 里的 user
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getName());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        userMapper.update(newUser);
    }

    private List<Project> getProjectListByWsAndUserId(String userId, String organizationId) {
        List<Project> projects = QueryChain.of(Project.class).where(PROJECT.ORGANIZATION_ID.eq(organizationId)
                .and(PROJECT.ENABLE.eq(true))).list();
        List<UserRoleRelation> userRoleRelations = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(userId)).list();
        List<Project> projectList = new ArrayList<>();
        userRoleRelations.forEach(userRoleRelation -> projects.forEach(project -> {
            if (StringUtils.equals(userRoleRelation.getSourceId(), project.getId())) {
                if (!projectList.contains(project)) {
                    projectList.add(project);
                }
            }
        }));
        return projectList;
    }

    private UserDTO getUserDTO(String id) {
        UserDTO response = QueryChain.of(User.class).where(User::getId).eq(id).oneAs(UserDTO.class);
        UserRolePermissionDTO dto = getUserRolePermission(id);
        response.setUserRoleRelations(dto.getUserRoleRelations());
        response.setUserRoles(dto.getUserRoles());
        response.setUserRolePermissions(dto.getList());
        return response;
    }

    private boolean hasLastOrganizationPermission(UserDTO user) {
        if (StringUtils.isNotBlank(user.getLastOrganizationId())) {
            List<Organization> organizations = QueryChain.of(Organization.class)
                    .where(ORGANIZATION.ID.eq(user.getLastOrganizationId()).and(ORGANIZATION.ENABLE.eq(true)))
                    .list();
            if (CollectionUtils.isEmpty(organizations)) {
                return false;
            }
            List<UserRoleRelation> userRoleRelations = user.getUserRoleRelations().stream()
                    .filter(ug -> StringUtils.equals(user.getLastOrganizationId(), ug.getSourceId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(userRoleRelations)) {
                List<Project> projects = QueryChain.of(Project.class)
                        .where(PROJECT.ORGANIZATION_ID.eq(user.getLastOrganizationId()).and(PROJECT.ENABLE.eq(true)))
                        .list();
                // 组织下没有项目
                if (CollectionUtils.isEmpty(projects)) {
                    UpdateChain.of(User.class).set(User::getLastProjectId, StringUtils.EMPTY)
                            .where(User::getId).eq(user.getId()).update();
                    return true;
                }
                // 组织下有项目，选中有权限的项目
                List<String> projectIds = projects.stream()
                        .map(Project::getId)
                        .toList();

                List<UserRoleRelation> roleRelations = user.getUserRoleRelations();
                List<String> projectRoleIds = user.getUserRoles()
                        .stream().filter(ug -> StringUtils.equals(ug.getType(), UserRoleType.PROJECT.name()))
                        .map(UserRole::getId)
                        .toList();
                List<String> projectIdsWithPermission = roleRelations.stream().filter(ug -> projectRoleIds.contains(ug.getRoleId()))
                        .map(UserRoleRelation::getSourceId)
                        .filter(StringUtils::isNotBlank)
                        .filter(projectIds::contains)
                        .toList();

                List<String> intersection = projectIds.stream().filter(projectIdsWithPermission::contains).collect(Collectors.toList());
                // 当前组织下的所有项目都没有权限
                if (CollectionUtils.isEmpty(intersection)) {
                    UpdateChain.of(User.class).set(User::getLastProjectId, StringUtils.EMPTY)
                            .where(User::getId).eq(user.getId()).update();
                    return true;
                }
                Optional<Project> first = projects.stream().filter(p -> StringUtils.equals(intersection.getFirst(), p.getId())).findFirst();
                if (first.isPresent()) {
                    Project project = first.get();
                    String wsId = project.getOrganizationId();
                    UpdateChain.of(User.class).set(User::getLastProjectId, project.getId())
                            .set(User::getLastOrganizationId, wsId)
                            .where(User::getId).eq(user.getId()).update();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasLastProjectPermission(UserDTO user) {
        if (StringUtils.isNotBlank(user.getLastProjectId())) {
            List<UserRoleRelation> userRoleRelations = user.getUserRoleRelations().stream()
                    .filter(ug -> StringUtils.equals(user.getLastProjectId(), ug.getSourceId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(userRoleRelations)) {
                List<Project> projects = QueryChain.of(Project.class)
                        .where(PROJECT.ID.eq(user.getLastProjectId()).and(PROJECT.ENABLE.eq(true)))
                        .list();
                if (CollectionUtils.isNotEmpty(projects)) {
                    Project project = projects.getFirst();
                    if (StringUtils.equals(project.getOrganizationId(), user.getLastOrganizationId())) {
                        return true;
                    }
                    // last_project_id 和 last_organization_id 对应不上了
                    UpdateChain.of(User.class).set(User::getLastOrganizationId, project.getOrganizationId())
                            .where(User::getId).eq(user.getId()).update();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSystemAdmin(UserDTO user) {
        boolean admin = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(user.getId())
                .and(USER_ROLE_RELATION.ROLE_ID.eq("admin"))).exists();
        if (admin) {
            if (StringUtils.isNotBlank(user.getLastProjectId())) {
                List<Project> projects = QueryChain.of(Project.class).where(Project::getId).eq(user.getLastProjectId())
                        .and(Project::getEnable).eq(true).list();
                if (CollectionUtils.isNotEmpty(projects)) {
                    Project project = projects.getFirst();
                    if (StringUtils.equals(project.getOrganizationId(), user.getLastOrganizationId())) {
                        return true;
                    }
                    UpdateChain.of(User.class).set(User::getLastOrganizationId, project.getOrganizationId())
                            .where(User::getId).eq(user.getId()).update();
                    return true;
                }
            }
            if (StringUtils.isNotBlank(user.getLastOrganizationId())) {
                List<Organization> organizations = QueryChain.of(Organization.class).where(Organization::getId).eq(user.getLastOrganizationId())
                        .and(Organization::getEnable).eq(true).list();
                if (CollectionUtils.isNotEmpty(organizations)) {
                    Organization organization = organizations.getFirst();
                    List<Project> projects = QueryChain.of(Project.class).where(Project::getOrganizationId).eq(organization.getId())
                            .and(Project::getEnable).eq(true).list();
                    if (CollectionUtils.isNotEmpty(projects)) {
                        Project project = projects.getFirst();
                        UpdateChain.of(User.class).set(User::getLastProjectId, project.getId())
                                .where(User::getId).eq(user.getId()).update();
                        return true;
                    }
                }
            }
            Project project = getEnableProjectAndOrganization();
            if (Objects.nonNull(project)) {
                UpdateChain.of(User.class).set(User::getLastProjectId, project.getId())
                        .set(User::getLastOrganizationId, project.getOrganizationId())
                        .where(User::getId).eq(user.getId()).update();
                return true;
            }
            return true;
        }
        return false;
    }

    private Project getEnableProjectAndOrganization() {
        return QueryChain.of(Project.class)
                .select(PROJECT.ALL_COLUMNS).from(PROJECT)
                .leftJoin(ORGANIZATION).on(ORGANIZATION.ID.eq(PROJECT.ORGANIZATION_ID))
                .where(PROJECT.ENABLE.eq(true).and(ORGANIZATION.ENABLE.eq(true)))
                .limit(1)
                .one();
    }
}
