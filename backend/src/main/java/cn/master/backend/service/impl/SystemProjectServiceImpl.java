package cn.master.backend.service.impl;

import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.mapper.ProjectTestResourcePoolMapper;
import cn.master.backend.mapper.UserMapper;
import cn.master.backend.mapper.UserRoleRelationMapper;
import cn.master.backend.payload.dto.system.OptionDTO;
import cn.master.backend.payload.dto.system.ProjectDTO;
import cn.master.backend.payload.request.system.AddProjectRequest;
import cn.master.backend.payload.request.system.ProjectAddMemberBatchRequest;
import cn.master.backend.payload.request.system.UpdateProjectRequest;
import cn.master.backend.service.BaseUserRolePermissionService;
import cn.master.backend.service.OperationLogService;
import cn.master.backend.service.SystemProjectService;
import cn.master.backend.util.Translator;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.master.backend.entity.table.ProjectTableDef.PROJECT;

/**
 * @author Created by 11's papa on 08/15/2024
 **/
@Service
public class SystemProjectServiceImpl extends ProjectServiceImpl implements SystemProjectService {
    public SystemProjectServiceImpl(ProjectTestResourcePoolMapper projectTestResourcePoolMapper,
                                    UserRoleRelationMapper userRoleRelationMapper,
                                    OperationLogService operationLogService,
                                    UserMapper userMapper,
                                    BaseUserRolePermissionService baseUserRolePermissionService) {
        super(projectTestResourcePoolMapper, userRoleRelationMapper, operationLogService, userMapper, baseUserRolePermissionService);
    }

    private final static String PREFIX = "/system/project";
    private final static String ADD_PROJECT = PREFIX + "/add";
    private final static String UPDATE_PROJECT = PREFIX + "/update";
    private final static String REMOVE_PROJECT_MEMBER = PREFIX + "/remove-member/";
    private final static String ADD_MEMBER = PREFIX + "/add-member";

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ProjectDTO add(AddProjectRequest addProjectDTO, String createUser) {
        return add(addProjectDTO, createUser, ADD_PROJECT, OperationLogModule.SETTING_SYSTEM_ORGANIZATION);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ProjectDTO update(UpdateProjectRequest updateProjectDto, String updateUser) {
        return update(updateProjectDto, updateUser, UPDATE_PROJECT, OperationLogModule.SETTING_SYSTEM_ORGANIZATION);
    }

    @Override
    public void addProjectMember(ProjectAddMemberBatchRequest request) {
        addProjectMember(request, ADD_MEMBER, OperationLogType.ADD.name(), Translator.get("add"), OperationLogModule.SETTING_SYSTEM_ORGANIZATION);
    }

    @Override
    public int removeProjectMember(String projectId, String userId, String createUser) {
        return removeProjectMember(projectId, userId, createUser, OperationLogModule.SETTING_SYSTEM_ORGANIZATION, StringUtils.join(REMOVE_PROJECT_MEMBER, projectId, "/", userId));
    }

    @Override
    public List<OptionDTO> list(String keyword) {
        return queryChain().where(PROJECT.ENABLE.eq(true)
                        .and(PROJECT.NAME.like(keyword)))
                .orderBy(PROJECT.CREATE_TIME.desc()).limit(1000)
                .listAs(OptionDTO.class);
    }
}
