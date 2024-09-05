import {
    SaveGlobalUSettingData,
    SystemUserGroupParams,
    UserGroupAuthSetting,
    UserGroupItem,
    UserTableItem
} from "/@/models/setting/user-group.ts";
import {alovaInstance} from "/@/api";
import * as ugUrl from "/@/api/req-urls/project-management/user-group.ts";
import {CommonPage, TableQueryParams} from "/@/models/common.ts";

/**
 * 项目-创建或修改用户组
 * @param param
 */
export const updateOrAddProjectUserGroup = (param: SystemUserGroupParams) => {
    return alovaInstance.Post<UserGroupItem>(param.id ? ugUrl.updateUrl : ugUrl.addUrl, param)
}
/**
 * 项目-获取用户组对应的权限
 * @param id
 */
export const getAuthByUserGroup = (id: string) => {
    return alovaInstance.Get<UserGroupAuthSetting[]>(`${ugUrl.listPermissionUrl}${id}`)
};
/**
 * 项目-编辑用户组对应的权限配置
 * @param param
 */
export const saveProjectUGSetting = (param: SaveGlobalUSettingData) => {
    return alovaInstance.Post<UserGroupAuthSetting[]>(ugUrl.updatePermissionUrl, param)
}
/**
 * 项目-获取用户组列表
 * @param param
 */
export const postUserGroupList = (param: TableQueryParams) => {
    return alovaInstance.Post<CommonPage<UserGroupItem>>(ugUrl.listUserGroupUrl, param)
}
/**
 * 项目-获取用户组对应的用户列表
 * @param param
 */
export const postUserByUserGroup = (param: TableQueryParams) => {
    return alovaInstance.Post<CommonPage<UserTableItem>>(ugUrl.listMemberUrl, param)
}
/**
 * 项目-获取需要关联的用户选项
 * @param projectId
 * @param userRoleId
 * @param keyword
 */
export const getProjectUserGroupOptions = (projectId: string, userRoleId: string, keyword: string) => {
    return alovaInstance.Get<UserTableItem[]>(`${ugUrl.getMemberOptionsUrl}${projectId}/${userRoleId}`, {params: {keyword}})
};
/**
 * 项目-添加用户到用户组
 * @param param
 */
export const addUserToUserGroup = (param: { projectId: string; userRoleId: string; userIds: string[] }) => {
    return alovaInstance.Post<string>(ugUrl.addMemberUrl, param)
}