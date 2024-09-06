import {CommonPage, TableQueryParams} from "/@/models/common.ts";
import {alovaInstance} from "/@/api";
import {AddOrUpdateMemberModel, LinkItem, MemberItem} from "/@/models/setting/member.ts";
import {
    AddMemberUrl,
    GetMemberListUrl,
    getProjectListUrl,
    getUserGroupList,
    getUserList,
    UpdateMemberUrl
} from "/@/api/req-urls/setting/member.ts";

/**
 * 获取成员列表
 * @param param
 */
export const getMemberList = (param: TableQueryParams) => alovaInstance.Post<CommonPage<MemberItem>>(GetMemberListUrl, param);
/**
 * 获取用户组下拉
 * @param organizationId
 */
export const getGlobalUserGroup = (organizationId: string) =>
    alovaInstance.Get<LinkItem[]>(`${getUserGroupList}/${organizationId}`, {});
/**
 * 获取组织下边的项目
 * @param organizationId
 * @param keyword
 */
export const getProjectList = (organizationId: string, keyword?: string) =>
    alovaInstance.Get<LinkItem[]>(`${getProjectListUrl}/${organizationId}`, {params: {keyword}});
/**
 * 获取组织下边的项目
 * @param organizationId
 * @param keyword
 */
export const getUser = (organizationId: string, keyword?: string) =>
    alovaInstance.Get<LinkItem[]>(`${getUserList}/${organizationId}`, {params: {keyword}});
/**
 * 添加成员
 * @param param
 * @param type
 */
export const addOrUpdate = (param: AddOrUpdateMemberModel, type: string) => {
    if (type === 'add') {
        return alovaInstance.Post(AddMemberUrl, param)
    }
    return alovaInstance.Post(UpdateMemberUrl, param)
}