import {AppRouteRecordRaw} from "/@/router/routers/types.ts";
import {TestPlanRouteEnum} from "/@/enums/route-enum.ts";
import {DEFAULT_LAYOUT} from "/@/router/routers/base.ts";

const TestPlan: AppRouteRecordRaw = {
    path: '/test-plan',
    name: TestPlanRouteEnum.TEST_PLAN,
    redirect: '/test-plan/testPlanIndex',
    component: DEFAULT_LAYOUT,
    meta: {
        locale: 'menu.testPlan',
        collapsedLocale: 'menu.testPlanShort',
        icon: 'icon-a-icon_test-tracking_filled1',
        order: 2,
        hideChildrenInMenu: true,
        roles: ['PROJECT_TEST_PLAN:READ', 'PROJECT_TEST_PLAN_REPORT:READ'],
    },
    children: [
        {
            path: 'testPlanIndex',
            name: TestPlanRouteEnum.TEST_PLAN_INDEX,
            component: () => import(`/@/views/test-plan/plan/index.vue`),
            meta: {
                locale: 'menu.testPlanShort',
                roles: ['PROJECT_TEST_PLAN:READ'],
                isTopMenu: true,
            },
        },
        {
            path: 'testPlanReport',
            name: TestPlanRouteEnum.TEST_PLAN_REPORT,
            component: () => import(`/@/views/test-plan/report/index.vue`),
            meta: {
                locale: 'menu.apiTest.report',
                roles: ['PROJECT_TEST_PLAN_REPORT:READ'],
                isTopMenu: true,
            },
        }
    ]
};
export default TestPlan;