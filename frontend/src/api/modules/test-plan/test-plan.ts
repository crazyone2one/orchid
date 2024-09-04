import {alovaInstance} from "/@/api";
import {CommonPage, ModuleTreeNode, TableQueryParams} from "/@/models/common.ts";
import {
    addTestPlanModuleUrl,
    AddTestPlanUrl,
    GetTestPlanListUrl,
    GetTestPlanModuleUrl,
    UpdateTestPlanUrl
} from "/@/api/req-urls/test-plan/testPlan.ts";
import {AddTestPlanParams, TestPlanItem} from "/@/models/test-plan/test-plan.ts";
import {CreateOrUpdateModule} from "/@/models/case-management/feature-case.ts";

/**
 * 获取模块树
 */
export const getTestPlanModule = (params: TableQueryParams) => alovaInstance.Get<Array<ModuleTreeNode>>(`${GetTestPlanModuleUrl}/${params.projectId}`);
/**
 * 创建测试计划
 * @param params
 */
export const addTestPlan = (params: AddTestPlanParams) => alovaInstance.Post<TestPlanItem>(`${AddTestPlanUrl}`, params)
/**
 * 更新测试计划
 * @param params
 */
export const updateTestPlan = (params: AddTestPlanParams) => alovaInstance.Post<TestPlanItem>(`${UpdateTestPlanUrl}`, params);
/**
 * 获取计划列表
 * @param params
 */
export const getTestPlanList = (params: TableQueryParams) => alovaInstance.Post<CommonPage<TestPlanItem>>(`${GetTestPlanListUrl}`, params);
/**
 * 创建模块树
 * @param params
 */
export const createPlanModuleTree = (params: CreateOrUpdateModule) => alovaInstance.Post(`${addTestPlanModuleUrl}`, params)