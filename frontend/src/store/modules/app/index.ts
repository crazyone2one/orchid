import {defineStore} from "pinia";

const useAppStore = defineStore('app', {
    state: () => {
        return {
            currentOrgId: '',
            currentProjectId: '',
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
    },
    persist: {
        paths: ['currentOrgId', 'currentProjectId', 'pageConfig', 'menuCollapse'],
    },
})
export default useAppStore;