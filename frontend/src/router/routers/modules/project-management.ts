import {AppRouteRecordRaw} from "/@/router/routers/types.ts";
import {DEFAULT_LAYOUT} from "/@/router/routers/base.ts";
import {ProjectManagementRouteEnum} from "/@/enums/route-enum.ts";

const ProjectManagement: AppRouteRecordRaw = {
    path: '/project-management',
    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT,
    redirect: '/project-management/permission',
    component: DEFAULT_LAYOUT,
    meta: {
        locale: 'menu.projectManagement',
        collapsedLocale: 'menu.projectManagementShort',
        icon: 'icon-icon_project-settings-filled',
        order: 1,
        hideChildrenInMenu: true,
        roles: [
            'PROJECT_BASE_INFO:READ',
            'PROJECT_TEMPLATE:READ',
            'PROJECT_FILE_MANAGEMENT:READ',
            'PROJECT_MESSAGE:READ',
            'PROJECT_CUSTOM_FUNCTION:READ',
            'PROJECT_LOG:READ',
            'PROJECT_ENVIRONMENT:READ',
            // 菜单管理
            'PROJECT_APPLICATION_WORKSTATION:READ',
            'PROJECT_APPLICATION_TEST_PLAN:READ',
            'PROJECT_APPLICATION_BUG:READ',
            'PROJECT_APPLICATION_CASE:READ',
            'PROJECT_APPLICATION_API:READ',
            'PROJECT_APPLICATION_UI:READ',
            'PROJECT_APPLICATION_PERFORMANCE_TEST:READ',
            // 菜单管理
            'PROJECT_USER:READ',
            'PROJECT_GROUP:READ',
        ],
    },
    children: [
        {
            path: 'permission',
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION,
            component: () => import('/@/views/project-management/project-permission/index.vue'),
            redirect: '/project-management/permission/basicInfo',
            meta: {
                locale: 'menu.projectManagement.projectPermission',
                roles: [
                    'PROJECT_BASE_INFO:READ',
                    // 菜单管理
                    'PROJECT_APPLICATION_WORKSTATION:READ',
                    'PROJECT_APPLICATION_TEST_PLAN:READ',
                    'PROJECT_APPLICATION_BUG:READ',
                    'PROJECT_APPLICATION_CASE:READ',
                    'PROJECT_APPLICATION_API:READ',
                    'PROJECT_APPLICATION_UI:READ',
                    'PROJECT_APPLICATION_PERFORMANCE_TEST:READ',
                    // 菜单管理
                    'PROJECT_USER:READ',
                    'PROJECT_GROUP:READ',
                ],
                isTopMenu: true,
            },
            children: [
                // 基本信息
                {
                    path: 'basicInfo',
                    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_BASIC_INFO,
                    component: () => import('/@/views/project-management/project-permission/basic-infos/index.vue'),
                    meta: {
                        locale: 'project.permission.basicInfo',
                        roles: ['PROJECT_BASE_INFO:READ'],
                    },
                },
                // 成员
                {
                    path: 'member',
                    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
                    component: () => import('/@/views/project-management/project-permission/member/index.vue'),
                    meta: {
                        locale: 'project.permission.member',
                        roles: ['PROJECT_USER:READ'],
                    },
                },
                // 用户组
                {
                    path: 'projectUserGroup',
                    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_USER_GROUP,
                    component: () => import('/@/views/project-management/project-permission/user-group/index.vue'),
                    meta: {
                        locale: 'project.permission.userGroup',
                        roles: ['PROJECT_GROUP:READ'],
                    },
                },
            ]
        },
        {
            path: 'projectManagementTemplate',
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE,
            component: () => import('/@/views/project-management/template/index.vue'),
            meta: {
                locale: 'menu.projectManagement.templateManager',
                roles: ['PROJECT_TEMPLATE:READ'],
                isTopMenu: true,
            },
        },
        // 模板列表-模板字段设置
        {
            path: 'projectManagementTemplateField',
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_FIELD_SETTING,
            component: () => import('/@/views/project-management/template/components/ProjectFieldSetting.vue'),
            meta: {
                locale: 'menu.settings.organization.templateFieldSetting',
                roles: ['PROJECT_TEMPLATE:READ'],
                breadcrumbs: [
                    {
                        name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE,
                        locale: 'menu.projectManagement.templateManager',
                    },
                    {
                        name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_FIELD_SETTING,
                        locale: 'menu.settings.organization.templateFieldSetting',
                        editLocale: 'menu.settings.organization.templateFieldSetting',
                        query: ['type'],
                    },
                ],
            },
        },
        {
            path: 'projectManagementTemplateList',
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT,
            component: () => import('/@/views/project-management/template/components/TemplateManagement.vue'),
            meta: {
                locale: 'menu.settings.organization.templateManagement',
                roles: ['PROJECT_TEMPLATE:READ'],
                breadcrumbs: [
                    {
                        name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE,
                        locale: 'menu.projectManagement.templateManager',
                    },
                    {
                        name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT,
                        locale: 'menu.settings.organization.templateManagementList',
                        editLocale: 'menu.settings.organization.templateManagementList',
                        query: ['type'],
                    },
                ],
            },
        }
    ]
};
export default ProjectManagement;