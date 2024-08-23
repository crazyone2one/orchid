import {defineStore} from "pinia";
import {RouteRecordRaw} from "vue-router";
import {AppState} from "/@/store/modules/app/types.ts";
import {cloneDeep} from "lodash-es";

const useAppStore = defineStore('app', {
    state: (): AppState => {
        return {
            currentOrgId: '',
            currentProjectId: '',
            currentMenuConfig: [],
            topMenus: [] as RouteRecordRaw[],
            currentTopMenu: {} as RouteRecordRaw,
        }
    },
    getters: {
        getCurrentOrgId(state: any): string {
            return state.currentOrgId;
        },
        getCurrentProjectId(state: any): string {
            return state.currentProjectId;
        },
    },
    actions: {
        /**
         * 设置当前组织 ID
         */
        setCurrentOrgId(id: string) {
            this.currentOrgId = id;
        },
        /**
         * 设置当前项目 ID
         */
        setCurrentProjectId(id: string) {
            this.currentProjectId = id;
        },
        getTopMenus(): RouteRecordRaw[] {
            return this.topMenus;
        },
        setTopMenus(menus: RouteRecordRaw[] | undefined) {
            this.topMenus = menus ? [...menus] : [];
        },
        getCurrentTopMenu(): RouteRecordRaw {
            return this.currentTopMenu;
        },
        /**
         * 设置激活的顶部菜单
         */
        setCurrentTopMenu(menu: RouteRecordRaw) {
            this.currentTopMenu = cloneDeep(menu);
        },
    },
    persist: {
        paths: ['currentOrgId', 'currentProjectId', 'pageConfig', 'menuCollapse'],
    },
})
export default useAppStore;