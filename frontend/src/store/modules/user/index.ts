import {defineStore} from 'pinia'
import {fetchLogin} from "/@/api/modules/login";
import {LoginData} from "/@/models/user.ts";
import useAppStore from "/@/store/modules/app";

const useUserStore = defineStore('main', {
    state: () => {
        return {
            name: undefined,
            userRolePermissions: [],
            userRoles: [],
            userRoleRelations: [],
            lastProjectId: '',
        }
    },
    actions: {
        // 重置用户信息
        resetInfo() {
            this.$reset();
        },
        // 设置用户信息
        setInfo(partial: Partial<any>) {
            this.$patch(partial);
        },
        async login(loginForm: LoginData) {
            const res = await fetchLogin(loginForm);
            // console.log(res)
            localStorage.setItem('access_token', res.access_token);
            localStorage.setItem('refresh_token', res.refresh_token);
            this.setInfo(res);
            const appStore = useAppStore();
            appStore.setCurrentOrgId(res.lastOrganizationId || '');
            appStore.setCurrentProjectId(res.lastProjectId || '');
        },
    },
    persist: true,
})

export default useUserStore