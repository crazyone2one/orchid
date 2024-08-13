package cn.master.backend.service.log;

import cn.master.backend.constants.HttpMethodConstants;
import cn.master.backend.constants.OperationLogConstants;
import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.entity.Organization;
import cn.master.backend.entity.User;
import cn.master.backend.entity.UserRole;
import cn.master.backend.mapper.OrganizationMapper;
import cn.master.backend.mapper.UserMapper;
import cn.master.backend.mapper.UserRoleMapper;
import cn.master.backend.payload.dto.LogDTOBuilder;
import cn.master.backend.payload.dto.TableBatchProcessDTO;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.dto.user.UserCreateInfo;
import cn.master.backend.payload.request.system.user.*;
import cn.master.backend.service.OperationLogService;
import cn.master.backend.service.UserToolService;
import cn.master.backend.util.JSON;
import cn.master.backend.util.Translator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Created by 11's papa on 08/13/2024
 **/
@Service
@RequiredArgsConstructor
public class UserLogService {
    private final UserToolService userToolService;
    private final OperationLogService operationLogService;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final OrganizationMapper organizationMapper;

    public LogDTO updateLog(UserEditRequest request) {
        User user = userMapper.selectOneById(request.getId());
        if (user != null) {

            return LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .type(OperationLogType.UPDATE.name())
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .method(HttpMethodConstants.POST.name())
                    .path("/system/user/update")
                    .sourceId(request.getId())
                    .content(user.getName())
                    .originalValue(JSON.toJSONBytes(user))
                    .build().getLogDTO();
        }
        return null;
    }

    public LogDTO updatePasswordLog(PersonalUpdatePasswordRequest request) {
        User user = userMapper.selectOneById(request.getId());
        if (user != null) {
            return LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .type(OperationLogType.UPDATE.name())
                    .module(OperationLogModule.PERSONAL_INFORMATION_PSW)
                    .method(HttpMethodConstants.POST.name())
                    .path("/personal/update-password")
                    .sourceId(request.getId())
                    .content(user.getName() + StringUtils.SPACE + Translator.get("personal.change.password"))
                    .originalValue(JSON.toJSONBytes(user))
                    .build().getLogDTO();
        }
        return null;
    }

    public List<LogDTO> getBatchAddLogs(@Valid List<UserCreateInfo> userList, String operator, String requestPath) {
        List<LogDTO> logs = new ArrayList<>();
        userList.forEach(user -> {
            LogDTO log = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .type(OperationLogType.ADD.name())
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .method(HttpMethodConstants.POST.name())
                    .path(requestPath)
                    .sourceId(user.getId())
                    .content(user.getName() + "(" + user.getEmail() + ")")
                    .originalValue(JSON.toJSONBytes(user))
                    .createUser(operator)
                    .build().getLogDTO();
            logs.add(log);
        });
        return logs;
    }

    public LogDTO updateAccountLog(PersonalUpdateRequest request) {
        User user = userMapper.selectOneById(request.getId());
        StringBuilder content = new StringBuilder();
        if (user != null) {
            if (!StringUtils.equals(user.getName(), request.getUsername())) {
                content.append(Translator.get("personal.user.name")).append(":").append(user.getName()).append("->").append(request.getUsername()).append(";");
            }
            if (!StringUtils.equals(user.getEmail(), request.getEmail())) {
                content.append(Translator.get("personal.user.email")).append(":").append(user.getEmail()).append("->").append(request.getEmail()).append(";");
            }
            if (!StringUtils.equals(user.getPhone(), request.getPhone())) {
                content.append(Translator.get("personal.user.phone")).append(":").append(user.getPhone()).append("->").append(request.getPhone()).append(";");
            }
            return LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .type(OperationLogType.UPDATE.name())
                    .module(OperationLogModule.PERSONAL_INFORMATION_BASE_INFO)
                    .method(HttpMethodConstants.POST.name())
                    .path("/personal/update-info")
                    .sourceId(request.getId())
                    .content(user.getName() + StringUtils.SPACE + Translator.get("personal.change.info") + ". " + content)
                    .originalValue(JSON.toJSONBytes(user))
                    .build().getLogDTO();
        }
        return null;
    }

    public List<LogDTO> batchUpdateEnableLog(UserChangeEnableRequest request) {
        List<LogDTO> logDTOList = new ArrayList<>();
        request.setSelectIds(userToolService.getBatchUserIds(request));
        List<User> userList = userToolService.selectByIdList(request.getSelectIds());
        for (User user : userList) {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .type(OperationLogType.UPDATE.name())
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .method(HttpMethodConstants.POST.name())
                    .path("/system/user/update/enable")
                    .sourceId(user.getId())
                    .content((request.isEnable() ? Translator.get("user.enable") : Translator.get("user.disable")) + ":" + user.getName())
                    .originalValue(JSON.toJSONBytes(user))
                    .build().getLogDTO();
            logDTOList.add(dto);
        }
        return logDTOList;
    }

    public List<LogDTO> resetPasswordLog(TableBatchProcessDTO request) {
        request.setSelectIds(userToolService.getBatchUserIds(request));
        List<LogDTO> returnList = new ArrayList<>();
        List<User> userList = userMapper.selectListByIds(request.getSelectIds());
        for (User user : userList) {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .type(OperationLogType.UPDATE.name())
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .method(HttpMethodConstants.POST.name())
                    .path("/system/user/reset/password")
                    .sourceId(user.getId())
                    .content(Translator.get("user.reset.password") + " : " + user.getName())
                    .originalValue(JSON.toJSONBytes(user))
                    .build().getLogDTO();
            returnList.add(dto);
        }
        return returnList;
    }

    public List<LogDTO> deleteLog(TableBatchProcessDTO request) {
        List<LogDTO> logDTOList = new ArrayList<>();
        request.getSelectIds().forEach(item -> {
            User user = userMapper.selectOneById(item);
            if (user != null) {
                LogDTO dto = LogDTOBuilder.builder()
                        .projectId(OperationLogConstants.SYSTEM)
                        .organizationId(OperationLogConstants.SYSTEM)
                        .type(OperationLogType.DELETE.name())
                        .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                        .method(HttpMethodConstants.POST.name())
                        .path("/system/user/delete")
                        .sourceId(user.getId())
                        .content(Translator.get("user.delete") + " : " + user.getName())
                        .originalValue(JSON.toJSONBytes(user))
                        .build().getLogDTO();
                logDTOList.add(dto);

            }
        });
        return logDTOList;
    }

    public void batchAddUserRoleLog(UserRoleBatchRelationRequest request, String operator) {
        List<LogDTO> logs = new ArrayList<>();
        List<String> userIds = userToolService.getBatchUserIds(request);
        List<User> userList = userToolService.selectByIdList(userIds);

        List<String> roleNameList = userRoleMapper.selectListByIds(request.getRoleIds())
                .stream().map(UserRole::getName).collect(Collectors.toList());
        String roleNames = StringUtils.join(roleNameList, ",");

        for (User user : userList) {
            //用户管理处修改了用户的组织。
            LogDTO log = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .createUser(operator)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .sourceId(user.getId())
                    .type(OperationLogType.UPDATE.name())
                    .content(user.getName() + Translator.get("user.add.group") + ":" + roleNames)
                    .path("/system/user/add/batch/user-role")
                    .method(HttpMethodConstants.POST.name())
                    .modifiedValue(JSON.toJSONBytes(request.getRoleIds()))
                    .build().getLogDTO();
            logs.add(log);
        }
        operationLogService.batchAdd(logs);
    }
    public void batchAddOrgLog(UserRoleBatchRelationRequest request, String operator) {
        List<LogDTO> logs = new ArrayList<>();
        List<String> userIds = userToolService.getBatchUserIds(request);
        List<User> userList = userToolService.selectByIdList(userIds);

        List<String> roleNameList = organizationMapper.selectListByIds(request.getRoleIds())
                .stream().map(Organization::getName).collect(Collectors.toList());
        String roleNames = StringUtils.join(roleNameList, ",");

        for (User user : userList) {
            //用户管理处修改了用户的组织。
            LogDTO log = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .createUser(operator)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .sourceId(user.getId())
                    .type(OperationLogType.UPDATE.name())
                    .content(user.getName() + Translator.get("user.add.org") + ":" + roleNames)
                    .path("/system/user/add-org-member")
                    .method(HttpMethodConstants.POST.name())
                    .modifiedValue(JSON.toJSONBytes(request.getRoleIds()))
                    .build().getLogDTO();
            logs.add(log);
        }
        operationLogService.batchAdd(logs);
    }
}
