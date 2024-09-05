import {alovaInstance} from "/@/api";
import {ProjectListUrl, ProjectSwitchUrl} from "/@/api/req-urls/project-management/project.ts";
import {OrgProjectTableItem, ProjectListItem} from "/@/models/orgAndProject.ts";

export const getProjectList = (organizationId: string) => alovaInstance.Get<Array<OrgProjectTableItem>>(`${ProjectListUrl}/${organizationId}`, {})
export const getProjectInfo = (projectId: string) => alovaInstance.Get<ProjectListItem>(`/project/get/${projectId}`, {})
export const switchProject = (data: { projectId: string; userId: string }) => alovaInstance.Post(ProjectSwitchUrl, data)