import {alovaInstance} from "/@/api";
import {
    OrgUserGroupParams,
    SaveGlobalUSettingData,
    SystemUserGroupParams,
    UserGroupAuthSetting,
    UserGroupItem,
    UserTableItem
} from "/@/models/setting/user-group.ts";
import * as ugUrl from '/@/api/req-urls/setting/user-group.ts';
import {CommonPage, TableQueryParams} from "/@/models/common.ts";
import {UserListItem} from "/@/models/setting/user.ts";

/**
 * 系统-获取用户组列表
 */
export const getUserGroupList = () => {
    return alovaInstance.Get<UserGroupItem[]>(ugUrl.getUserGroupU)
}
/**
 * 组织-获取用户组列表
 * @param organizationId
 */
export const getOrgUserGroupList = (organizationId: string) => {
    return alovaInstance.Get<UserGroupItem[]>(`${ugUrl.getOrgUserGroupU}${organizationId}`)
}
/**
 * // 项目-获取用户组列表
 * @param projectId
 */
export const getProjectUserGroupList = (projectId: string) => {
    return alovaInstance.Get<UserGroupItem[]>(`${ugUrl.getProjectUserGroupU}${projectId}`)
}
export const getSystemUserGroupOption = (id: string, keyword: string) => {
    return alovaInstance.Get<UserListItem[]>(`${ugUrl.getSystemUserGroupOptionUrl}${id}`, {params: {keyword}})
}
/**
 * 组织-获取需要关联的用户选项
 * @param organizationId
 * @param roleId
 * @param keyword
 */
export const getOrgUserGroupOption = (organizationId: string, roleId: string, keyword: string) => {
    return alovaInstance.Get<UserListItem[]>(`${ugUrl.getOrgUserGroupOptionUrl}${organizationId}/${roleId}`, {params: {keyword}})
}
/**
 * 系统-创建或修改用户组
 * @param param
 */
export const updateOrAddUserGroup = (param: SystemUserGroupParams) => {
    return alovaInstance.Post<UserGroupItem>(param.id ? ugUrl.updateUserGroupU : ugUrl.addUserGroupU, param)
}
/**
 * 组织-创建或修改用户组
 * @param param
 */
export const updateOrAddOrgUserGroup = (param: OrgUserGroupParams) => {
    return alovaInstance.Post<UserGroupItem>(param.id ? ugUrl.updateOrgUserGroupU : ugUrl.addOrgUserGroupU, param)
}
/**
 * 系统-获取用户组对应的用户列表
 * @param param
 */
export const postUserByUserGroup = (param: TableQueryParams) => {
    return alovaInstance.Post<CommonPage<UserTableItem[]>>(ugUrl.postUserByUserGroupUrl, param)
}
/**
 * 组织-获取用户组对应的用户列表
 * @param param
 */
export const postOrgUserByUserGroup = (param: TableQueryParams) => {
    return alovaInstance.Post<CommonPage<UserTableItem[]>>(ugUrl.postOrgUserByUserGroupUrl, param)
}

export const getGlobalUSetting = (id: string) => {
    return alovaInstance.Get<UserGroupAuthSetting[]>(`${ugUrl.getGlobalUSettingUrl}${id}`)
}
/**
 * 组织-获取用户组对应的权限配置
 * @param id
 */
export const getOrgUSetting = (id: string) => {
    return alovaInstance.Get<UserGroupAuthSetting[]>(`${ugUrl.getOrgUSettingUrl}${id}`)
}
/**
 * 系统-编辑用户组对应的权限配置
 * @param param
 */
export const saveGlobalUSetting = (param: SaveGlobalUSettingData) => {
    return alovaInstance.Post<UserGroupAuthSetting[]>(ugUrl.editGlobalUSettingUrl, param)
}
/**
 * 组织-编辑用户组对应的权限配置
 * @param param
 */
export const saveOrgUSetting = (param: SaveGlobalUSettingData) => {
    return alovaInstance.Post<UserGroupAuthSetting[]>(ugUrl.editOrgUSettingUrl, param)
}
/**
 * 系统-添加用户到用户组
 * @param param
 */
export const addUserToUserGroup = (param: { roleId: string; userIds: string[] }) => {
    return alovaInstance.Post<string>(ugUrl.addUserToUserGroupUrl, param)
}
/**
 * 组织-添加用户到用户组
 * @param param
 */
export const addOrgUserToUserGroup = (param: { userRoleId: string; userIds: string[]; organizationId: string }) => {
    return alovaInstance.Post<string>(ugUrl.addOrgUserToUserGroupUrl, param)
}
/**
 * 系统-删除用户组对应的用户
 * @param id
 */
export const deleteUserFromUserGroup = (id: string) => {
    return alovaInstance.Get<string>(`${ugUrl.deleteUserFromUserGroupUrl}${id}`)
}
/**
 * 组织-删除用户组对应的用户
 * @param param
 */
export const deleteOrgUserFromUserGroup = (param: {
    userRoleId: string;
    userIds: string[];
    organizationId: string
}) => {
    return alovaInstance.Post<CommonPage<UserTableItem[]>>(ugUrl.deleteOrgUserFromUserGroupUrl, param)
}