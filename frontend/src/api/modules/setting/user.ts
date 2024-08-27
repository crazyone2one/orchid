import {alovaInstance} from "/@/api";
import {CommonPage, TableQueryParams} from "/@/models/common.ts";
import {
    CreateUserParams,
    CreateUserResult,
    SystemRole,
    UpdateUserInfoParams, UpdateUserStatusParams,
    UserListItem
} from "/@/models/setting/user.ts";
import {CreateUserUrl, EnableUserUrl, GetSystemRoleUrl, GetUserListUrl, UpdateUserUrl} from "/@/api/req-urls/user.ts";

/**
 * 获取用户列表
 * @param param
 */
export const getUserList = (param: TableQueryParams) => alovaInstance.Post<CommonPage<UserListItem>>(GetUserListUrl, param, {
    transform(data: any, _headers) {
        return data.records.map((item: UserListItem) => ({
            ...item,
            selectUserGroupVisible: false,
            selectUserGroupLoading: false,
            userGroupIds: item.userRoleList?.map(tmp => tmp.id)
        }));
    }
})
/**
 * 获取系统用户组
 */
export const getSystemRoles = () => alovaInstance.Get<Array<SystemRole>>(GetSystemRoleUrl)
export const updateUserInfo = (param: UpdateUserInfoParams) => alovaInstance.Post(UpdateUserUrl, param)
export const batchCreateUser = (param: CreateUserParams) => alovaInstance.Post<CreateUserResult>(CreateUserUrl, param)
export const toggleUserStatus = (param: UpdateUserStatusParams) => alovaInstance.Post(EnableUserUrl, param)