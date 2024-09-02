import {alovaInstance} from "/@/api";
import {ProjectBasicInfoUrl} from "/@/api/req-urls/project-management/basicInfo.ts";
import {ProjectBasicInfoModel} from "/@/models/project-management/basicInfo.ts";

export const getProjectInfo = (id: string) => alovaInstance.Get<ProjectBasicInfoModel>(`${ProjectBasicInfoUrl}/${id}`, {})