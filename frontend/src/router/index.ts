import {createRouter, createWebHashHistory, RouteRecordRaw} from "vue-router";
import appRoutes from "/@/router/routers";
import {INDEX_ROUTE} from "/@/router/routers/base.ts";
import createRouteGuard from "/@/router/guard";

const routes: Array<RouteRecordRaw> = [
    {
        path: "/",
        name: "Home",
        component: () => import(`/@/layout/index.vue`),
    },
    // {
    //     path: '/',
    //     redirect: 'login',
    // },
    {
        path: '/login',
        name: 'login',
        component: () => import(`/@/views/login/index.vue`),
        meta: {
            requiresAuth: false,
        },
    },
    ...appRoutes,
    INDEX_ROUTE
];
const router = createRouter({
    history: createWebHashHistory(),
    routes,
    scrollBehavior() {
        return {top: 0};
    },
});
createRouteGuard(router);

export default router;
