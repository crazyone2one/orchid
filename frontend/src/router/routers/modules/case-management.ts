import {AppRouteRecordRaw} from "/@/router/routers/types.ts";
import {DEFAULT_LAYOUT} from "/@/router/routers/base.ts";
import {CaseManagementRouteEnum} from "/@/enums/route-enum.ts";

const CaseManagement: AppRouteRecordRaw = {
    path: '/case-management',
    name: CaseManagementRouteEnum.CASE_MANAGEMENT,
    redirect: '/case-management/featureCase',
    component: DEFAULT_LAYOUT,
    meta: {
        locale: 'menu.caseManagement',
        collapsedLocale: 'menu.caseManagementShort',
        icon: 'icon-icon_functional_testing1',
        order: 3,
        hideChildrenInMenu: true,
        roles: ['FUNCTIONAL_CASE:READ', 'CASE_REVIEW:READ'],
    },
    children: [
        {
            path: 'featureCase',
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE,
            component: () => import('/@/views/case-management/case-management-feature/index.vue'),
            meta: {
                locale: 'menu.caseManagementShort',
                roles: ['FUNCTIONAL_CASE:READ'],
                isTopMenu: true,
            },
        },
        {
            path: 'review',
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW,
            component: () => import('/@/views/case-management/case-review/index.vue'),
            meta: {
                locale: 'menu.caseManagement.caseManagementReviewShort',
                roles: ['CASE_REVIEW:READ'],
                isTopMenu: true,
            },
        },
    ],
};
export default CaseManagement;