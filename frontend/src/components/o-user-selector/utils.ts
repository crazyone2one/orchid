import {
    getAdminByOrganizationOrProject,
    getAdminByProjectByOrg,
    getUserByOrganizationOrProject
} from "/@/api/modules/setting/system-org-project.ts";
import {getOrgUserGroupOption, getSystemUserGroupOption} from "/@/api/modules/setting/user-group.ts";
import {useRequest} from "alova/client";
import {getProjectUserGroupOptions} from "/@/api/modules/project-management/user-group.ts";

export enum UserRequestTypeEnum {
    SYSTEM_USER_GROUP = 'SYSTEM_USER_GROUP',
    SYSTEM_ORGANIZATION = 'SYSTEM_ORGANIZATION',
    SYSTEM_ORGANIZATION_ADMIN = 'SYSTEM_ORGANIZATION_ADMIN',
    SYSTEM_PROJECT = 'SYSTEM_PROJECT',
    SYSTEM_PROJECT_ADMIN = 'SYSTEM_PROJECT_ADMIN',
    ORGANIZATION_USER_GROUP = 'ORGANIZATION_USER_GROUP',
    ORGANIZATION_USER_GROUP_ADMIN = 'ORGANIZATION_USER_GROUP_ADMIN',
    ORGANIZATION_PROJECT = 'ORGANIZATION_PROJECT',
    ORGANIZATION_PROJECT_ADMIN = 'ORGANIZATION_PROJECT_ADMIN',
    SYSTEM_ORGANIZATION_PROJECT = 'SYSTEM_ORGANIZATION_PROJECT',
    SYSTEM_ORGANIZATION_MEMBER = 'SYSTEM_ORGANIZATION_MEMBER',
    PROJECT_PERMISSION_MEMBER = 'PROJECT_PERMISSION_MEMBER',
    PROJECT_USER_GROUP = 'PROJECT_USER_GROUP',
    SYSTEM_ORGANIZATION_LIST = 'SYSTEM_ORGANIZATION_LIST',
    SYSTEM_PROJECT_LIST = 'SYSTEM_PROJECT_LIST',
    EXECUTE_USER = 'EXECUTE_USER',
}

export default function initOptionsFunc(type: string, params: Record<string, any>) {
    if (type === UserRequestTypeEnum.SYSTEM_ORGANIZATION_ADMIN || type === UserRequestTypeEnum.SYSTEM_PROJECT_ADMIN) {
        // 系统 - 【组织 或 项目】-添加管理员-下拉选项
        return useRequest(() => getAdminByOrganizationOrProject(params.keyword), {force: true}).send();
    }
    if (type === UserRequestTypeEnum.SYSTEM_USER_GROUP) {
        // 系统 - 用户组-添加成员-下拉选项
        return useRequest(() => getSystemUserGroupOption(params.roleId, params.keyword), {force: true}).send();
    }
    if (type === UserRequestTypeEnum.ORGANIZATION_USER_GROUP) {
        // 组织 - 用户组-添加成员-下拉选项
        return useRequest(() => getOrgUserGroupOption(params.organizationId, params.roleId, params.keyword), {force: true}).send();
    }
    if (type === UserRequestTypeEnum.ORGANIZATION_PROJECT_ADMIN) {
        return useRequest(() => getAdminByProjectByOrg(params.organizationId, params.keyword), {force: true}).send();
    }
    if (type === UserRequestTypeEnum.PROJECT_USER_GROUP) {
        return useRequest(() => getProjectUserGroupOptions(params.projectId, params.userRoleId, params.keyword), {force: true}).send();
    }
    if (type === UserRequestTypeEnum.SYSTEM_ORGANIZATION || type === UserRequestTypeEnum.SYSTEM_PROJECT) {
        // 系统 -【组织 或 项目】-添加成员-下拉选项
        return useRequest(() => getUserByOrganizationOrProject(params.sourceId, params.keyword), {force: true}).send();
    }
}