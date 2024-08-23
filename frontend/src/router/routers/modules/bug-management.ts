import {AppRouteRecordRaw} from "/@/router/routers/types.ts";
import {DEFAULT_LAYOUT} from "/@/router/routers/base.ts";
import {BugManagementRouteEnum} from "/@/enums/route-enum.ts";

const BugManagement: AppRouteRecordRaw = {
    path: '/bug-management',
    name: BugManagementRouteEnum.BUG_MANAGEMENT,
    redirect: '/bug-management/index',
    component: DEFAULT_LAYOUT,
    meta: {
        locale: 'menu.bugManagement',
        collapsedLocale: 'menu.bugManagementShort',
        icon: 'icon-icon_defect',
        order: 7,
        roles: ['PROJECT_BUG:READ'],
        hideChildrenInMenu: true,
    },
    children: [
        {
            path: 'index',
            name: BugManagementRouteEnum.BUG_MANAGEMENT_INDEX,
            component: () => import('/@/views/bug-management/index.vue'),
            meta: {
                locale: 'bugManagement.index',
                roles: ['PROJECT_BUG:READ'],
                isTopMenu: true,
            },
        }
    ]
};
export default BugManagement;