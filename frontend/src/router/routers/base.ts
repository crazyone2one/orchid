import {RouteRecordRaw} from "vue-router";

export const DEFAULT_LAYOUT = () => import(`/@/layout/index.vue`);
export const NO_PERMISSION_LAYOUT = () => import(`/@/layout/NoPermission.vue`);
export const INDEX_ROUTE: RouteRecordRaw = {
    path: '/index',
    name: 'orchidIndex',
    component: NO_PERMISSION_LAYOUT,
    meta: {
        hideInMenu: true,
        roles: ['*'],
        requiresAuth: true,
    },
};
export const NOT_FOUND_ROUTE: RouteRecordRaw = {
    path: '/:pathMatch(.*)*',
    name: 'notFound',
    component: () => import('/@/views/base/not-found/index.vue'),
};