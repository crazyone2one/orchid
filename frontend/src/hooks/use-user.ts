import {useAppStore, useUserStore} from "/@/store";
import router from "/@/router";

export default function useUser() {
    // const { t } = useI18n();
    const logout = async (logoutTo?: string, noRedirect?: boolean) => {
        const userStore = useUserStore();
        await userStore.logout();
        const appStore = useAppStore();
        const currentRoute = router.currentRoute.value;
        // 清空顶部菜单
        appStore.setTopMenus([]);
        window.$message.success('登出成功');
        router.push({
            name: logoutTo && typeof logoutTo === 'string' ? logoutTo : 'login',
            query: noRedirect
                ? {}
                : {
                    ...router.currentRoute.value.query,
                    redirect: currentRoute.name as string,
                },
        });
    }
    const isLoginPage = () => {
        return window.location.hash.indexOf('login') > -1;
    };
    return {
        logout,
        isLoginPage,
        // isWhiteListPage,
    };
}