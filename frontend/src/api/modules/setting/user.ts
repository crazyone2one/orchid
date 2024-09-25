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
import {ViewDetail, ViewList, ViewParams} from "/@/components/o-advance-filter/type.ts";

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
/**
 * 视图列表
 */
export const getViewList = (viewType: string, scopeId: string) => alovaInstance.Get<ViewList>(`/user-view/${viewType}/grouped/list`, {
    params: {scopeId}
})
export const getViewDetail = (viewType: string, id: string) => alovaInstance.Get<ViewDetail>(`/user-view/${viewType}/get/${id}`);
/**
 * 删除视图
 * @param viewType
 * @param id
 */
export const deleteView = (viewType: string, id: string) => alovaInstance.Get(`/user-view/${viewType}/delete/${id}`);
/**
 * 编辑视图
 * @param viewType
 * @param param
 */
export const updateView = (viewType: string, param: ViewParams) => alovaInstance.Post(`/user-view/${viewType}/update`, param);
export const addView = (viewType: string, param: ViewParams) => alovaInstance.Post(`/user-view/${viewType}/add`, param);