import {defineStore} from 'pinia'
import {fetchLogin, fetchLogout, fetchUserIsLogin} from "/@/api/modules/login";
import {LoginData} from "/@/models/user.ts";
import useAppStore from "/@/store/modules/app";
import {UserState} from "/@/store/modules/user/types.ts";
import {composePermissions, getFirstRouteNameByPermission} from "/@/utils/permission.ts";
import {clearToken, setToken} from "/@/utils/auth.ts";
import {removeRouteListener} from "/@/utils/route-listener.ts";
import {getHashParameters, getQueryVariable} from "/@/utils";
import router from "/@/router";
import {getUserHasProjectPermission} from "/@/api/modules/system.ts";
import {getProjectInfo} from "/@/api/modules/project-management/project.ts";


const useUserStore = defineStore('main', {
    state: (): UserState => {
        return {
            name: undefined,
            avatar: undefined,
            job: undefined,
            organization: undefined,
            location: undefined,
            email: undefined,
            introduction: undefined,
            personalWebsite: undefined,
            jobName: undefined,
            organizationName: undefined,
            locationName: undefined,
            phone: undefined,
            registrationDate: undefined,
            id: undefined,
            certification: undefined,
            role: '',
            userRolePermissions: [],
            userRoles: [],
            userRoleRelations: [],
            loginType: [],
            hasLocalExec: false, // 是否配置了api本地执行
            isPriorityLocalExec: false, // 是否优先本地执行
            localExecuteUrl: '',
            lastProjectId: '',
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
        },
        /**
         * 判断用户是否登录并设置用户信息
         * @param forceSet
         */
        async isLogin(forceSet = false) {
            try {
                const res = await fetchUserIsLogin();
                if (!res) {
                    return false;
                }
                const appStore = useAppStore();
                setToken(res.access_token, res.refresh_token);
                this.setInfo(res);
                let {orgId, pId} = getHashParameters();
                if (!pId) {
                    pId = getQueryVariable('_pId') || '';
                }
                if (!orgId) {
                    orgId = getQueryVariable('_orgId') || '';
                }
                if (!forceSet && orgId) {
                    appStore.setCurrentOrgId(orgId);
                }
                if (!forceSet && pId) {
                    appStore.setCurrentProjectId(pId);
                }
                if (forceSet) {
                    appStore.setCurrentOrgId(res.lastOrganizationId || '');
                    appStore.setCurrentProjectId(res.lastProjectId || '');
                }
                return true;
            } catch (err) {
                console.log(err);
                return false;
            }
        },
        async checkIsLogin(forceSet = false) {
            const appStore = useAppStore();
            const isLogin = await this.isLogin(forceSet);
            if (isLogin && appStore.currentProjectId !== 'no_such_project') {
                // 当前为登陆状态，且已经选择了项目，初始化当前项目配置
                try {
                    const HasProjectPermission = await getUserHasProjectPermission(appStore.currentProjectId);
                    if (!HasProjectPermission) {
                        // 没有项目权限（用户所在的当前项目被禁用&用户被移除出去该项目）
                        router.push({
                            name: "no project",
                        });
                        return;
                    }
                    const routeName = router.currentRoute.value.name as string;
                    if (routeName?.includes('setting')) {
                        // 访问系统设置下的页面，不需要获取项目信息，会在切换到非系统设置页面时获取(ms-menu组件内初始化会获取)
                        await appStore.setCurrentMenuConfig([]);
                        return;
                    }
                    const res = await getProjectInfo(appStore.currentProjectId);
                    if (!res) {
                        // 如果项目被删除或者被禁用，跳转到无项目页面
                        router.push({
                            name: 'no-project',
                        });
                    }

                    if (res) {
                        appStore.setCurrentMenuConfig(res?.moduleIds || []);
                    }
                } catch (err) {
                    await appStore.setCurrentMenuConfig([]);
                    console.log(err);
                }
            } else if (isLogin && appStore.currentProjectId === 'no_such_project') {
                await router.push({
                    name: 'no-project',
                });
                throw new Error('no project');
            }
            if (window.location.hash.indexOf('login') > -1 && isLogin) {
                // 当前页面为登录页面，且已经登录，跳转到首页
                const currentRouteName = getFirstRouteNameByPermission(router.getRoutes());
                await router.push({name: currentRouteName});
            }
        },
    },
    // persist: true,
    persist: {
        paths: ['name', 'id', 'userRolePermissions', 'userRoleRelations', 'lastProjectId'],
    },
})

export default useUserStore