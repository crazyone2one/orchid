package cn.master.backend.service.impl;

import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.entity.Organization;
import cn.master.backend.entity.User;
import cn.master.backend.entity.UserRoleRelation;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.mapper.ProjectTestResourcePoolMapper;
import cn.master.backend.mapper.UserMapper;
import cn.master.backend.mapper.UserRoleRelationMapper;
import cn.master.backend.payload.dto.system.ProjectDTO;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.system.*;
import cn.master.backend.service.BaseUserRolePermissionService;
import cn.master.backend.service.OperationLogService;
import cn.master.backend.service.OrganizationProjectService;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.SelectQueryTable;
import jakarta.transaction.Transactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static cn.master.backend.entity.table.UserTableDef.USER;

/**
 * @author Created by 11's papa on 08/14/2024
 **/
@Service
public class OrganizationProjectServiceImpl extends ProjectServiceImpl implements OrganizationProjectService {
    private final static String PREFIX = "/organization-project";
    private final static String ADD_PROJECT = PREFIX + "/add";
    private final static String UPDATE_PROJECT = PREFIX + "/update";
    private final static String REMOVE_PROJECT_MEMBER = PREFIX + "/remove-member/";
    private final static String ADD_MEMBER = PREFIX + "/add-member";

    public OrganizationProjectServiceImpl(ProjectTestResourcePoolMapper projectTestResourcePoolMapper,
                                          UserRoleRelationMapper userRoleRelationMapper,
                                          OperationLogService operationLogService,
                                          UserMapper userMapper,
                                          BaseUserRolePermissionService baseUserRolePermissionService) {
        super(projectTestResourcePoolMapper, userRoleRelationMapper, operationLogService, userMapper, baseUserRolePermissionService);
    }


    @Override
    public ProjectDTO add(AddProjectRequest addProjectDTO, String createUser) {
        return super.add(addProjectDTO, createUser, ADD_PROJECT, OperationLogModule.SETTING_ORGANIZATION_PROJECT);
    }

    @Override
    public Page<ProjectDTO> getProjectPage(OrganizationProjectRequest request) {
        ProjectRequest projectRequest = new ProjectRequest();
        BeanUtils.copyProperties(request, projectRequest);
        return super.getProjectPage(projectRequest);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ProjectDTO update(UpdateProjectRequest updateProjectDto, String updateUser) {
        return update(updateProjectDto, updateUser, UPDATE_PROJECT, OperationLogModule.SETTING_ORGANIZATION_PROJECT);
    }

    @Override
    public void addProjectMember(ProjectAddMemberBatchRequest request) {
        addProjectMember(request, ADD_MEMBER, OperationLogType.ADD.name(), Translator.get("add"), OperationLogModule.SETTING_ORGANIZATION_PROJECT);
    }

    @Override
    public int removeProjectMember(String projectId, String userId, String createUser) {
        return removeProjectMember(projectId, userId, createUser, OperationLogModule.SETTING_ORGANIZATION_PROJECT, StringUtils.join(REMOVE_PROJECT_MEMBER, projectId, "/", userId));
    }

    @Override
    public List<UserExtendDTO> getUserAdminList(String organizationId, String keyword) {
        checkOrgIsExist(organizationId);
        return QueryChain.of(User.class)
                .select(QueryMethods.distinct(USER.ID, USER.NAME, USER.EMAIL))
                .from(USER).leftJoin(USER_ROLE_RELATION).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId))
                .and(USER.NAME.like(keyword).or(USER.EMAIL.like(keyword)))
                .orderBy(USER.CREATE_TIME.desc()).limit(1000)
                .listAs(UserExtendDTO.class);
    }

    @Override
    public List<UserExtendDTO> getUserMemberList(String organizationId, String projectId, String keyword) {
        checkOrgIsExist(organizationId);
        checkProjectNotExist(projectId);
        List<UserRoleRelation> userRoleRelations = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId)).list();
        List<String> userIds = userRoleRelations.stream().map(UserRoleRelation::getUserId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(userIds)) {
            QueryChain<UserRoleRelation> userRoleRelationQueryChain = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(projectId));
            return QueryChain.of(User.class).select(QueryMethods.distinct(USER.ID, USER.NAME, USER.EMAIL))
                    .select("count(temp.id) > 0 as memberFlag")
                    .from(USER.as("u"))
                    .leftJoin(new SelectQueryTable(userRoleRelationQueryChain).as("temp"))
                    .on("temp.user_id = u.id")
                    .where(USER.ID.in(userIds))
                    .and(USER.NAME.like(keyword).or(USER.EMAIL.like(keyword)))
                    .groupBy(USER.ID)
                    .orderBy(USER.CREATE_TIME.desc()).limit(1000).listAs(UserExtendDTO.class);
        }
        return List.of();
    }

    private void checkOrgIsExist(String organizationId) {
        QueryChain.of(Organization.class).where(Organization::getId).eq(organizationId).oneOpt()
                .orElseThrow(() -> new MSException(Translator.get("organization_not_exists")));
    }

}
