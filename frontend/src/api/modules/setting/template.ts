import {alovaInstance} from "/@/api";
import {
    CreateFieldUrl,
    CreateProjectFieldUrl,
    DeleteFieldDetailUrl,
    DeleteProjectFieldDetailUrl, EnableOrOffTemplateUrl,
    GetDefinedFieldListUrl,
    GetDefinedProjectFieldListUrl,
    GetFieldDetailUrl,
    GetFieldProjectDetailUrl,
    getOrdTemplateStateUrl,
    getProjectTemplateStateUrl, GetProjectTemplateUrl,
    UpdateFieldUrl,
    UpdateProjectFieldUrl
} from "/@/api/req-urls/setting/template.ts";
import {TableQueryParams} from "/@/models/common.ts";
import {AddOrUpdateField, SceneType} from "/@/models/setting/template.ts";

/**
 * 获取模板列表的状态(组织)
 * @param scopedId
 */
export const getOrdTemplate = (scopedId: string) => alovaInstance.Get<Record<string, boolean>>(`${getOrdTemplateStateUrl}/${scopedId}`);
/**
 * 获取模板列表的状态(项目)
 * @param scopedId
 */
export const getProTemplate = (scopedId: string) => alovaInstance.Get<Record<string, boolean>>(`${getProjectTemplateStateUrl}/${scopedId}`)
/**
 * 获取自定义字段列表(组织)
 * @param params
 */
export const getFieldList = (params: TableQueryParams) =>
    alovaInstance.Get(`${GetDefinedFieldListUrl}/${params.scopedId}/${params.scene}`)
/**
 * // 获取自定义字段列表(组织)
 * @param params
 */
export const getProjectFieldList = (params: TableQueryParams) =>
    alovaInstance.Get(`${GetDefinedProjectFieldListUrl}/${params.scopedId}/${params.scene}`)

export const deleteOrdField = (id: string) => alovaInstance.Get<Record<string, boolean>>(`${DeleteFieldDetailUrl}/${id}`)
export const deleteProjectField = (id: string) => alovaInstance.Get<Record<string, boolean>>(`${DeleteProjectFieldDetailUrl}/${id}`)
/**
 * 获取自定义字段详情选项(组织)
 * @param id
 */
export const getOrdFieldDetail = (id: string) => alovaInstance.Get<Record<string, boolean>>(`${GetFieldDetailUrl}/${id}`)
export const getProjectFieldDetail = (id: string) => alovaInstance.Get<Record<string, boolean>>(`${GetFieldProjectDetailUrl}/${id}`)
/**
 * 创建自定义字段(组织)
 * @param params
 */
export const addOrUpdateOrdField = (params: AddOrUpdateField) => {
    return alovaInstance.Post<AddOrUpdateField>(params.id ? UpdateFieldUrl : CreateFieldUrl, params)
}

export const addOrUpdateProjectField = (params: AddOrUpdateField) => {
    return alovaInstance.Post(params.id ? UpdateProjectFieldUrl : CreateProjectFieldUrl, params)
};
/**
 * 关闭组织模板||开启项目模板
 * @param organizationId
 * @param scene
 */
export const enableOrOffTemplate = (organizationId: string, scene: SceneType) => alovaInstance.Get(`${EnableOrOffTemplateUrl}/${organizationId}/${scene}`);
/**
 * 获取模板列表(项目)
 * @param params
 */
export const getProjectTemplateList = (params: TableQueryParams) => alovaInstance.Get(`${GetProjectTemplateUrl}/${params.projectId}/${params.scene}`)