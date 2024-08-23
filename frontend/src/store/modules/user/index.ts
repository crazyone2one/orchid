import {defineStore} from 'pinia'
import {fetchLogin, fetchLogout} from "/@/api/modules/login";
import {LoginData} from "/@/models/user.ts";
import useAppStore from "/@/store/modules/app";
import {UserState} from "/@/store/modules/user/types.ts";
import {composePermissions} from "/@/utils/permission.ts";
import {clearToken} from "/@/utils/auth.ts";
import {removeRouteListener} from "/@/utils/route-listener.ts";


const useUserStore = defineStore('main', {
    state: (): UserState => {
        return {
            name: undefined,
            userRolePermissions: [],
            userRoles: [],
            userRoleRelations: [],
            lastProjectId: '',
            lastOrganizationId: '',
        }
    },
    getters: {
        userInfo(state: UserState): UserState {
            return {...state};
        },
        isAdmin(state: UserState): boolean {
            if (!state.userRolePermissions) return false;
            return state.userRolePermissions.findIndex((ur) => ur.userRole.id === 'admin') > -1;
        },
        currentRole(state: UserState): {
            projectPermissions: string[];
            orgPermissions: string[];
            systemPermissions: string[];
        } {
            const appStore = useAppStore();

            state.userRoleRelations?.forEach((ug) => {
                state.userRolePermissions?.forEach((gp) => {
                    if (gp.userRole.id === ug.roleId) {
                        ug.userRolePermissions = gp.userRolePermissions;
                        ug.userRole = gp.userRole;
                    }
                });
            });

            return {
                projectPermissions: composePermissions(state.userRoleRelations || [], 'PROJECT', appStore.currentProjectId),
                orgPermissions: composePermissions(state.userRoleRelations || [], 'ORGANIZATION', appStore.currentOrgId),
                systemPermissions: composePermissions(state.userRoleRelations || [], 'SYSTEM', 'global'),
            };
        },
    },
    actions: {
        // 重置用户信息
        resetInfo() {
            this.$reset();
        },
        // 设置用户信息
        setInfo(partial: Partial<UserState>) {
            this.$patch(partial);
        },
        async login(loginForm: LoginData) {
            try {
                const res = await fetchLogin(loginForm);
                // console.log(res)
                localStorage.setItem('access_token', res.access_token);
                localStorage.setItem('refresh_token', res.refresh_token);
                this.setInfo(res);
                const appStore = useAppStore();
                appStore.setCurrentOrgId(res.lastOrganizationId || '');
                appStore.setCurrentProjectId(res.lastProjectId || '');
            } catch (e) {
                clearToken()
            }
        },
        async logout() {
            await fetchLogout();
            clearToken();
            this.resetInfo();
            removeRouteListener();
        }
    },
    persist: true,
})

export default useUserStore