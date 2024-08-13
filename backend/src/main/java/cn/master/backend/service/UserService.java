package cn.master.backend.service;

import cn.master.backend.payload.dto.BasePageRequest;
import cn.master.backend.payload.dto.TableBatchProcessDTO;
import cn.master.backend.payload.dto.user.UserBatchCreateResponse;
import cn.master.backend.payload.dto.user.UserDTO;
import cn.master.backend.payload.request.system.user.UserBatchCreateRequest;
import cn.master.backend.payload.request.system.user.UserChangeEnableRequest;
import cn.master.backend.payload.request.system.user.UserEditRequest;
import cn.master.backend.payload.response.TableBatchProcessResponse;
import cn.master.backend.payload.response.user.UserTableResponse;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.User;
import jakarta.validation.Valid;

/**
 * 用户 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-06
 */
public interface UserService extends IService<User> {

    UserEditRequest updateUser(UserEditRequest request, String currentUserId);

    UserDTO getUserByKeyword(String keyword);

    UserBatchCreateResponse addUser(UserBatchCreateRequest request, String source, String operator);

    Page<UserTableResponse> page(BasePageRequest request);

    TableBatchProcessResponse updateUserEnable(UserChangeEnableRequest request, String currentUserId, String username);

    TableBatchProcessResponse deleteUser(@Valid TableBatchProcessDTO request, String operatorId, String operatorName);

    TableBatchProcessResponse resetPassword(TableBatchProcessDTO request, String operator);
}
