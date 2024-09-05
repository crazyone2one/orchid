import {RouteRecordRaw} from "vue-router";
import {OrgProjectTableItem} from "/@/models/orgAndProject.ts";
import {BreadcrumbItem} from "/@/components/o-breadcrumb/types.ts";

export interface AppState {
    topMenus: RouteRecordRaw[];
    currentTopMenu: RouteRecordRaw;
    currentOrgId: string;
    currentProjectId: string;
    currentMenuConfig: string[];
    projectList: OrgProjectTableItem[];
    innerHeight: number;
    breadcrumbList: BreadcrumbItem[];
}