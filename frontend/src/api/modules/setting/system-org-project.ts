import {alovaInstance} from "/@/api";
import {CommonPage, TableQueryParams} from "/@/models/common.ts";
import * as orgUrl from '/@/api/req-urls/system-org-project.ts'
import {OrganizationListItem} from "/@/models/organization.ts";
import {OrgProjectTableItem, SystemOrgOption} from "/@/models/orgAndProject.ts";
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


export const getAdminByOrganizationOrProject = (keyword: string) => alovaInstance.Get<Array<UserListItem>>(`${orgUrl.getAdminByOrgOrProjectUrl}`, {params: {keyword}})