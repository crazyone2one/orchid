import {alovaInstance} from "/@/api";
import {CommonPage, TableQueryParams} from "/@/models/common.ts";
import * as orgUrl from '/@/api/req-urls/system-org-project.ts'
import {OrganizationListItem} from "/@/models/organization.ts";
import {AddUserToOrgOrProjectParams, OrgProjectTableItem, SystemOrgOption} from "/@/models/orgAndProject.ts";
import {UserListItem} from "/@/models/setting/user.ts";

/**
 * 获取组织列表
 * @param param
 */
export const postOrgTable = (param: TableQueryParams) => alovaInstance.Post<CommonPage<OrganizationListItem>>(orgUrl.postOrgTableUrl, param);
/**
 * 系统-获取项目列表
 * @param param
 */
export const postProjectTable = (param: TableQueryParams) => alovaInstance.Post<CommonPage<OrgProjectTableItem>>(orgUrl.postProjectTableUrl, param);
/**
 * 获取组织下拉选项
 */
export const getSystemOrgOption = () => alovaInstance.Post<Array<SystemOrgOption>>(orgUrl.postOrgOptionsUrl);
export const createOrUpdateProject = (param: Partial<OrgProjectTableItem>) =>
    alovaInstance.Post<OrgProjectTableItem>(param.id ? orgUrl.postModifyProjectUrl : orgUrl.postAddProjectUrl, param);
/**
 * 启用或禁用组织
 * @param id
 * @param isEnable
 */
export const enableOrDisableOrg = (id: string, isEnable = true) =>
    alovaInstance.Get(`${isEnable ? orgUrl.getEnableOrgUrl : orgUrl.getDisableOrgUrl}${id}`)
/**
 * 启用或禁用项目
 * @param id
 * @param isEnable
 */
export const enableOrDisableProject = (id: string, isEnable = true) =>
    alovaInstance.Get(`${isEnable ? orgUrl.getEnableProjectUrl : orgUrl.getDisableProjectUrl}${id}`)

/**
 * 系统-获取管理员下拉选项
 * @param keyword
 */
export const getAdminByOrganizationOrProject = (keyword: string) => alovaInstance.Get<Array<UserListItem>>(`${orgUrl.getAdminByOrgOrProjectUrl}`, {params: {keyword}});
/**
 * 组织-获取项目下的管理员选项
 * @param organizationId
 * @param keyword
 */
export const getAdminByProjectByOrg = (organizationId: string, keyword: string) =>
    alovaInstance.Get<Array<UserListItem>>(`${orgUrl.getAdminByOrganizationOrProjectUrl}${organizationId}`, {params: {keyword}});
/**
 * 获取项目和组织的总数
 */
export const getOrgAndProjectCount = () => alovaInstance.Get<{
    projectTotal: number,
    organizationTotal: number
}>(`${orgUrl.getOrgAndProjectCountUrl}`);
/**
 * 给组织或项目添加成员
 * @param param
 */
export const addUserToOrgOrProject = (param: AddUserToOrgOrProjectParams) =>
    alovaInstance.Post<OrgProjectTableItem>(param.projectId ? orgUrl.postAddProjectMemberUrl : orgUrl.postAddOrgMemberUrl, param);
/**
 * 获取用户下拉选项
 * @param sourceId
 * @param keyword
 */
export const getUserByOrganizationOrProject = (sourceId: string, keyword: string) =>
    alovaInstance.Get<Array<UserListItem>>(`${orgUrl.getUserByOrgOrProjectUrl}${sourceId}`, {params: {keyword}});