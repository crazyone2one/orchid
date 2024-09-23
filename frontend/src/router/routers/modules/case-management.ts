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
        // 功能用例
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
        // 创建用例&编辑用例
        {
            path: 'featureCaseDetail/:mode?',
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
            component: () => import('/@/views/case-management/case-management-feature/components/CaseDetail.vue'),
            meta: {
                locale: 'menu.caseManagement.featureCaseDetail',
                roles: ['FUNCTIONAL_CASE:READ+ADD', 'FUNCTIONAL_CASE:READ+UPDATE'],
                breadcrumbs: [
                    {
                        name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE,
                        locale: 'menu.caseManagement.featureCase',
                    },
                    {
                        name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
                        locale: 'menu.caseManagement.featureCaseDetail',
                        editTag: 'id',
                        editLocale: 'menu.caseManagement.featureCaseEdit',
                    },
                ],
            },
        },
        // 创建用例成功
        {
            path: 'featureCaseCreateSuccess',
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_CREATE_SUCCESS,
            component: () => import('/@/views/case-management/case-management-feature/components/CreateSuccess.vue'),
            meta: {
                locale: 'menu.caseManagement.featureCaseCreateSuccess',
                roles: ['FUNCTIONAL_CASE:READ+ADD'],
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
        {
            path: 'caseManagementReviewCreate',
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_CREATE,
            component: () => import('/@/views/case-management/case-review/components/CreateReview.vue'),
            meta: {
                locale: 'menu.caseManagement.caseManagementReviewCreate',
                roles: ['CASE_REVIEW:READ+ADD', 'CASE_REVIEW:READ+UPDATE'],
                breadcrumbs: [
                    {
                        name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW,
                        locale: 'menu.caseManagement.caseManagementReview',
                    },
                    {
                        name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_CREATE,
                        locale: 'menu.caseManagement.caseManagementReviewCreate',
                        editTag: 'id',
                        editLocale: 'menu.caseManagement.caseManagementCaseReviewEdit',
                    },
                ],
            },
        },
    ],
};
export default CaseManagement;