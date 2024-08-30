import {RouteRecordRaw} from "vue-router";
import {OrgProjectTableItem} from "/@/models/orgAndProject.ts";

export interface AppState {
    topMenus: RouteRecordRaw[];
    currentTopMenu: RouteRecordRaw;
    currentOrgId: string;
    currentProjectId: string;
    currentMenuConfig: string[];
    projectList: OrgProjectTableItem[];
}