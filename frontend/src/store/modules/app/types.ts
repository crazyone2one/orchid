import {RouteRecordRaw} from "vue-router";

export interface AppState {
    topMenus: RouteRecordRaw[];
    currentTopMenu: RouteRecordRaw;
    currentOrgId: string;
    currentProjectId: string;
    currentMenuConfig: string[];
}