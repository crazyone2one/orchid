import {AppRouteRecordRaw} from "/@/router/routers/types.ts";
import {DEFAULT_LAYOUT} from "/@/router/routers/base.ts";
import {SettingRouteEnum} from "/@/enums/route-enum.ts";

const Setting: AppRouteRecordRaw = {
    path: '/setting',
    name: SettingRouteEnum.SETTING,
    component: DEFAULT_LAYOUT,
    meta: {
        locale: 'menu.settings',
        collapsedLocale: 'menu.settingsShort',
        icon: 'icon-a-icon_system_settings',
        order: 8,
        roles: [
            'SYSTEM_USER:READ',
            'SYSTEM_USER_ROLE:READ',
            'SYSTEM_ORGANIZATION_PROJECT:READ',
            'SYSTEM_PARAMETER_SETTING_BASE:READ',
            'SYSTEM_PARAMETER_SETTING_DISPLAY:READ',
            'SYSTEM_PARAMETER_SETTING_AUTH:READ',
            'SYSTEM_PARAMETER_SETTING_MEMORY_CLEAN:READ',
            'SYSTEM_PARAMETER_SETTING_QRCODE:READ',
            'SYSTEM_TEST_RESOURCE_POOL:READ',
            'SYSTEM_AUTH:READ',
            'SYSTEM_PLUGIN:READ',
            'SYSTEM_LOG:READ',
            'ORGANIZATION_MEMBER:READ',
            'ORGANIZATION_USER_ROLE:READ',
            'ORGANIZATION_PROJECT:READ',
            'SYSTEM_SERVICE_INTEGRATION:READ',
            'ORGANIZATION_TEMPLATE:READ',
            'ORGANIZATION_LOG:READ',
            'SYSTEM_TASK_CENTER:READ',
        ],
    },
    children: [
        {
            path: 'system',
            name: SettingRouteEnum.SETTING_SYSTEM,
            component: null,
            meta: {
                locale: 'menu.settings.system',
                roles: [
                    'SYSTEM_USER:READ',
                    'SYSTEM_USER_ROLE:READ',
                    'SYSTEM_ORGANIZATION_PROJECT:READ',
                    'SYSTEM_PARAMETER_SETTING_BASE:READ',
                    'SYSTEM_PARAMETER_SETTING_DISPLAY:READ',
                    'SYSTEM_PARAMETER_SETTING_AUTH:READ',
                    'SYSTEM_PARAMETER_SETTING_MEMORY_CLEAN:READ',
                    'SYSTEM_PARAMETER_SETTING_QRCODE:READ',
                    'SYSTEM_TEST_RESOURCE_POOL:READ',
                    'SYSTEM_AUTH:READ',
                    'SYSTEM_PLUGIN:READ',
                    'SYSTEM_LOG:READ',
                    'SYSTEM_TASK_CENTER:READ',
                ],
                hideChildrenInMenu: true,
            },
            children: [
                {
                    path: 'user',
                    name: SettingRouteEnum.SETTING_SYSTEM_USER_SINGLE,
                    component: () => import('/@/views/setting/system/user/index.vue'),
                    meta: {
                        locale: 'menu.settings.system.user',
                        roles: ['SYSTEM_USER:READ'],
                        isTopMenu: true,
                    },
                },
                {
                    path: 'usergroup',
                    name: SettingRouteEnum.SETTING_SYSTEM_USER_GROUP,
                    component: () => import('/@/views/setting/system/usergroup/index.vue'),
                    meta: {
                        locale: 'menu.settings.system.usergroup',
                        roles: ['SYSTEM_USER_ROLE:READ'],
                        isTopMenu: true,
                    },
                },
                {
                    path: 'organization-and-project',
                    name: SettingRouteEnum.SETTING_SYSTEM_ORGANIZATION,
                    component: () => import('/@/views/setting/system//organizationAndProject/index.vue'),
                    meta: {
                        locale: 'menu.settings.system.organizationAndProject',
                        roles: ['SYSTEM_ORGANIZATION_PROJECT:READ'],
                        isTopMenu: true,
                    },
                },
            ]
        },
        {
            path: 'organization',
            name: SettingRouteEnum.SETTING_ORGANIZATION,
            redirect: '',
            component: null,
            meta: {
                locale: 'menu.settings.organization',
                roles: [
                    'ORGANIZATION_MEMBER:READ',
                    'ORGANIZATION_USER_ROLE:READ',
                    'ORGANIZATION_PROJECT:READ',
                    'SYSTEM_SERVICE_INTEGRATION:READ',
                    'ORGANIZATION_TEMPLATE:READ',
                    'ORGANIZATION_LOG:READ',
                    'ORGANIZATION_TASK_CENTER:READ',
                    'ORGANIZATION_TASK_CENTER:READ',
                ],
                hideChildrenInMenu: true,
            },
            children: [
                {
                    path: 'member',
                    name: SettingRouteEnum.SETTING_ORGANIZATION_MEMBER,
                    component: () => import('/@/views/setting/organization/member/index.vue'),
                    meta: {
                        locale: 'menu.settings.organization.member',
                        roles: ['ORGANIZATION_MEMBER:READ'],
                        isTopMenu: true,
                    },
                },
                {
                    path: 'usergroup',
                    name: SettingRouteEnum.SETTING_ORGANIZATION_USER_GROUP,
                    component: () => import('/@/views/setting/organization/usergroup/index.vue'),
                    meta: {
                        locale: 'menu.settings.organization.userGroup',
                        roles: ['ORGANIZATION_USER_ROLE:READ'],
                        isTopMenu: true,
                    },
                },
                {
                    path: 'project',
                    name: SettingRouteEnum.SETTING_ORGANIZATION_PROJECT,
                    component: () => import('/@/views/setting/organization/project/index.vue'),
                    meta: {
                        locale: 'menu.settings.organization.project',
                        roles: ['ORGANIZATION_PROJECT:READ'],
                        isTopMenu: true,
                    },
                },
                {
                    path: 'template',
                    name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE,
                    component: () => import('/@/views/setting/organization/template/index.vue'),
                    meta: {
                        locale: 'menu.settings.organization.template',
                        roles: ['ORGANIZATION_TEMPLATE:READ'],
                        isTopMenu: true,
                    },
                },
            ]
        }
    ]
};
export default Setting;