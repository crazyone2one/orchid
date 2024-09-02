import {
    SaveGlobalUSettingData,
    SystemUserGroupParams,
    UserGroupAuthSetting,
    UserGroupItem
} from "/@/models/setting/user-group.ts";
import {alovaInstance} from "/@/api";
import * as ugUrl from "/@/api/req-urls/project-management/user-group.ts";

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