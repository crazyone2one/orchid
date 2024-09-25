import {alovaInstance} from "/@/api";
import {ProjectMemberList} from "/@/api/req-urls/project-management/project-member.ts";

export const getProjectOptions = (projectId: string, keyword?: string) => alovaInstance.Get(`${ProjectMemberList}/${projectId}`, {params: {keyword}})