import {alovaInstance} from "/@/api";
import {CommonPage, TableQueryParams} from "/@/models/common.ts";
import {SystemRole, UpdateUserInfoParams, UserListItem} from "/@/models/setting/user.ts";
import {GetSystemRoleUrl, GetUserListUrl, UpdateUserUrl} from "/@/api/req-urls/user.ts";

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