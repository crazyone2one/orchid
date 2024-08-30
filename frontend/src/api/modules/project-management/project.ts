import {alovaInstance} from "/@/api";
import {ProjectListUrl} from "/@/api/req-urls/project-management/project.ts";
import {OrgProjectTableItem} from "/@/models/orgAndProject.ts";

export const getProjectList = (organizationId: string) => alovaInstance.Get<Array<OrgProjectTableItem>>(`${ProjectListUrl}/${organizationId}`,{})