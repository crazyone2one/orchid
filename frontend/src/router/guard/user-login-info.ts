import type {LocationQueryRaw, Router} from "vue-router";
import {hasToken} from "/@/utils/auth.ts";

export default function setupUserLoginInfoGuard(router: Router) {

    router.beforeEach(async (to, _from, next) => {
        if (to.name !== 'login' && hasToken(to.name as string)) {
            next();
        } else {
            if (to.name === 'login') {
                next();
                return;
            }
            // 未登录的且访问非白名单内的地址都直接跳转至登录页，访问的页面地址缓存到 query 上
            next({
                name: 'login',
                query: {
                    redirect: to.name,
                    ...to.query,
                } as LocationQueryRaw,
            });
        }
    })
};