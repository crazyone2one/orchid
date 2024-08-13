package cn.master.backend.service.impl;

import cn.master.backend.constants.UserRoleScope;
import cn.master.backend.entity.User;
import cn.master.backend.entity.UserRoleRelation;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.handler.result.SystemResultCode;
import cn.master.backend.mapper.UserMapper;
import cn.master.backend.payload.dto.BasePageRequest;
import cn.master.backend.payload.dto.TableBatchProcessDTO;
import cn.master.backend.payload.dto.user.UserBatchCreateResponse;
import cn.master.backend.payload.dto.user.UserCreateInfo;
import cn.master.backend.payload.dto.user.UserDTO;
import cn.master.backend.payload.request.system.user.UserBatchCreateRequest;
import cn.master.backend.payload.request.system.user.UserChangeEnableRequest;
import cn.master.backend.payload.request.system.user.UserEditRequest;
import cn.master.backend.payload.response.TableBatchProcessResponse;
import cn.master.backend.payload.response.user.UserTableResponse;
import cn.master.backend.service.*;
import cn.master.backend.service.log.UserLogService;
import cn.master.backend.util.JSON;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;

import java.util.*;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.UserTableDef.USER;

/**
 * 用户 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-06
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final GlobalUserRoleService globalUserRoleService;
    private final GlobalUserRoleRelationService globalUserRoleRelationService;
    private final PasswordEncoder passwordEncoder;
    private final OperationLogService operationLogService;
    private final UserLogService userLogService;
    private final UserToolService userToolService;
    @Value("50MB")
    private DataSize maxImportFileSize;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public UserEditRequest updateUser(UserEditRequest request, String currentUserId) {
        globalUserRoleService.checkRoleIsGlobalAndHaveMember(request.getUserRoleIdList(), true);
        checkUserEmail(request.getId(), request.getEmail());
        User user = new User();
        BeanUtils.copyProperties(request, user);
        mapper.update(user);
        globalUserRoleRelationService.updateUserSystemGlobalRole(user, user.getUpdateUser(), request.getUserRoleIdList());
        return request;
    }

    @Override
    public UserDTO getUserByKeyword(String keyword) {
        UserDTO one = queryChain().where(USER.EMAIL.like(keyword).or(USER.ID.like(keyword))).oneAs(UserDTO.class);
        if (Objects.nonNull(one)) {
            one.setUserRoleRelations(
                    globalUserRoleRelationService.selectByUserId(one.getId())
            );
            one.setUserRoles(
                    globalUserRoleService.selectByUserRoleRelations(one.getUserRoleRelations())
            );
        }
        return one;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public UserBatchCreateResponse addUser(UserBatchCreateRequest request, String source, String operator) {
        globalUserRoleService.checkRoleIsGlobalAndHaveMember(request.getUserRoleIdList(), true);
        UserBatchCreateResponse response = new UserBatchCreateResponse();
        Map<String, String> errorEmails = this.validateUserInfo(request.getUserInfoList().stream().map(UserCreateInfo::getEmail).toList());
        if (MapUtils.isNotEmpty(errorEmails)) {
            response.setErrorEmails(errorEmails);
            throw new MSException(SystemResultCode.INVITE_EMAIL_EXIST, JSON.toJSONString(errorEmails.keySet()));
        } else {
            response.setSuccessList(this.saveUserAndRole(request, source, operator, "/system/user/addUser"));
        }
        return response;
    }

    @Override
    public Page<UserTableResponse> page(BasePageRequest request) {
        QueryChain<User> queryChain = queryChain()
                .where(USER.ID.eq(request.getKeyword())
                        .or(USER.NAME.like(request.getKeyword())
                                .or(USER.EMAIL.like(request.getKeyword())
                                        .or(USER.PHONE.like(request.getKeyword())))));
        Page<UserTableResponse> responsePage =
                mapper.paginateAs(Page.of(request.getCurrent(), request.getPageSize()), queryChain, UserTableResponse.class);
        List<UserTableResponse> userList = responsePage.getRecords();
        if (CollectionUtils.isNotEmpty(userList)) {
            List<String> userIdList = userList.stream().map(User::getId).toList();
            Map<String, UserTableResponse> roleAndOrganizationMap = globalUserRoleRelationService.selectGlobalUserRoleAndOrganization(userIdList);
            for (UserTableResponse user : userList) {
                UserTableResponse userInfo = new UserTableResponse();
                BeanUtils.copyProperties(user, userInfo);
                UserTableResponse roleOrgModel = roleAndOrganizationMap.get(user.getId());
                if (roleOrgModel != null) {
                    userInfo.setUserRoleList(roleOrgModel.getUserRoleList());
                    userInfo.setOrganizationList(roleOrgModel.getOrganizationList());
                }
            }
        }
        return responsePage;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public TableBatchProcessResponse updateUserEnable(UserChangeEnableRequest request, String operatorId, String operatorName) {
        request.setSelectIds(userToolService.getBatchUserIds(request));
        checkUserInDb(request.getSelectIds());
        if (!request.isEnable()) {
            //不能禁用当前用户和admin
            checkProcessUserAndThrowException(request.getSelectIds(), operatorId, operatorName, Translator.get("user.not.disable"));
        }
        val update = updateChain().set(User::getEnable, request.isEnable())
                .where(User::getId).in(request.getSelectIds()).update();
        TableBatchProcessResponse response = new TableBatchProcessResponse();
        if (update) {
            response.setTotalCount(request.getSelectIds().size());
            response.setSuccessCount(request.getSelectIds().size());
        }
        return response;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public TableBatchProcessResponse deleteUser(TableBatchProcessDTO request, String operatorId, String operatorName) {
        List<String> userIdList = userToolService.getBatchUserIds(request);
        this.checkUserInDb(userIdList);
        //检查是否含有Admin
        this.checkProcessUserAndThrowException(userIdList, operatorId, operatorName, Translator.get("user.not.delete"));
        TableBatchProcessResponse response = new TableBatchProcessResponse();
        response.setTotalCount(userIdList.size());
        response.setSuccessCount(mapper.deleteBatchByIds(userIdList));
        //删除用户角色关系
        globalUserRoleRelationService.deleteByUserIdList(userIdList);
        //todo 批量踢出用户
        //userIdList.forEach(SessionUtils::kickOutUser);
        return response;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public TableBatchProcessResponse resetPassword(TableBatchProcessDTO request, String operator) {
        request.setSelectIds(userToolService.getBatchUserIds(request));
        this.checkUserInDb(request.getSelectIds());
        List<User> userList = userToolService.selectByIdList(request.getSelectIds());
        userList.forEach(user -> {
            if (StringUtils.equalsIgnoreCase("admin", user.getId())) {
                user.setPassword(passwordEncoder.encode("orchid"));
            } else {
                user.setPassword(passwordEncoder.encode(user.getEmail()));
            }
            user.setUpdateUser(operator);
            mapper.update(user);
        });
        TableBatchProcessResponse response = new TableBatchProcessResponse();
        response.setTotalCount(request.getSelectIds().size());
        response.setSuccessCount(request.getSelectIds().size());
        return response;
    }

    private void checkProcessUserAndThrowException(List<String> userIdList, String operatorId, String operatorName, String exceptionMessage) {
        for (String userId : userIdList) {
            //当前用户或admin不能被操作
            if (StringUtils.equals(userId, operatorId)) {
                throw new MSException(exceptionMessage + ":" + operatorName);
            } else if (StringUtils.equals(userId, "admin")) {
                throw new MSException(exceptionMessage + ": admin");
            }
        }
    }

    private void checkUserInDb(@Valid List<String> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            throw new MSException(Translator.get("user.not.exist"));
        }
        long count = queryChain().where(USER.ID.in(userIdList)).count();
        if (userIdList.size() != count) {
            throw new MSException(Translator.get("user.not.exist"));
        }
    }

    private List<UserCreateInfo> saveUserAndRole(UserBatchCreateRequest request, String source, String operator, String requestPath) {
        List<@Valid UserCreateInfo> userInfoList = request.getUserInfoList();

        for (@Valid UserCreateInfo userCreateInfo : userInfoList) {
            User user = User.builder().name(userCreateInfo.getName())
                    .password(passwordEncoder.encode(userCreateInfo.getEmail()))
                    .email(userCreateInfo.getEmail())
                    .phone(userCreateInfo.getPhone())
                    .enable(false).source(source).avatar("").cftToken("NONE")
                    .createUser(operator).updateUser(operator)
                    .build();
            mapper.insert(user);
            List<String> userRoleIdList = request.getUserRoleIdList();
            if (!userRoleIdList.isEmpty()) {
                for (String roleId : userRoleIdList) {
                    UserRoleRelation userRoleRelation = UserRoleRelation.builder()
                            .userId(user.getId()).roleId(roleId).createUser(operator)
                            .sourceId(UserRoleScope.SYSTEM)
                            .organizationId(UserRoleScope.SYSTEM)
                            .build();
                    globalUserRoleRelationService.save(userRoleRelation);
                }
            }

        }
        operationLogService.batchAdd(userLogService.getBatchAddLogs(request.getUserInfoList(), operator, requestPath));
        return request.getUserInfoList();
    }

    private Map<String, String> validateUserInfo(List<String> list) {
        Map<String, String> errorMessage = new HashMap<>();
        String userEmailRepeatError = Translator.get("user.email.repeat");
        //判断参数内是否含有重复邮箱
        List<String> emailList = new ArrayList<>();
        Map<String, String> userInDbMap = queryChain().select(USER.ID, USER.EMAIL).where(USER.EMAIL.in(list)).list()
                .stream().collect(Collectors.toMap(User::getEmail, User::getId));
        list.forEach(email -> {
            if (emailList.contains(email)) {
                errorMessage.put(email, userEmailRepeatError);
            } else {
                //判断邮箱是否已存在数据库中
                if (userInDbMap.containsKey(email)) {
                    errorMessage.put(email, userEmailRepeatError);
                } else {
                    emailList.add(email);
                }
            }
        });
        return errorMessage;
    }

    private void checkUserEmail(String id, String email) {
        long count = queryChain().where(User::getEmail).eq(email).and(User::getId).ne(id).count();
        if (count > 0) {
            throw new MSException(Translator.get("user_email_already_exists"));
        }
    }
}
