import {alovaInstance} from "/@/api";
import {uploadInstance} from "/@/api/upload.ts";
import {CommonPage, ModuleTreeNode, TableQueryParams} from "/@/models/common.ts";
import {
    CreateCaseModuleTreeUrl,
    CreateCaseUrl,
    DetailCaseUrl,
    GetCaseListUrl,
    GetCaseModulesCountUrl,
    GetCaseModuleTreeUrl,
    GetDefaultTemplateFieldsUrl,
    GetRecycleCaseModulesCountUrl
} from "/@/api/req-urls/case-management/feature-case.ts";
import {CaseManagementTable, CreateOrUpdateModule} from "/@/models/case-management/feature-case.ts";
import {ContentTypeEnum} from "/@/enums/http-enum.ts";

/**
 * 获取全部用例模块数量
 * @param params
 */
export const getCaseModulesCounts = (params: TableQueryParams) => {
    return alovaInstance.Post<Record<string, any>>(GetCaseModulesCountUrl, params);
};
/**
 * 获取回收站模块数量
 * @param params
 */
export const getRecycleModulesCounts = (params: TableQueryParams) => {
    return alovaInstance.Post<Record<string, any>>(GetRecycleCaseModulesCountUrl, params);
};
/**
 * 获取默认模板自定义字段
 * @param projectId
 */
export const getCaseDefaultFields = (projectId: string) => {
    return alovaInstance.Get<Record<string, any>>(`${GetDefaultTemplateFieldsUrl}/${projectId}`)
};
/**
 * 用例详情
 * @param id
 */
export const getCaseDetail = (id: string) => {
    return alovaInstance.Get<Record<string, any>>(`${DetailCaseUrl}/${id}`)
};
/**
 * 获取模块树
 * @param projectId
 */
export const getCaseModuleTree = (projectId: string) => {
    return alovaInstance.Get<Array<ModuleTreeNode>>(`${GetCaseModuleTreeUrl}/${projectId}`)
};
/**
 *  创建模块树
 * @param params
 */
export const createCaseModuleTree = (params: CreateOrUpdateModule) => {
    return alovaInstance.Post(CreateCaseModuleTreeUrl, params);
};

export const getCaseList = (params: TableQueryParams) => {
    return alovaInstance.Post<CommonPage<CaseManagementTable>>(GetCaseListUrl, params);
};

export const createCaseRequest = (params: Record<string, any>) => {
    const formData = new FormData();
    params.fileList.forEach((item: any) => {
        formData.append("files", item.file, item.file.name);
    });
    if (params.request) {
        const requestData = JSON.stringify(params.request);
        formData.append('request', new Blob([requestData], {type: ContentTypeEnum.JSON}));
    }
    return uploadInstance.Post<CreateOrUpdateModule>(CreateCaseUrl, formData);
};