import {TableQueryParams} from "/@/models/common.ts";
import {alovaInstance} from "/@/api";
import * as bugURL from '/@/api/req-urls/bug-management.ts'

export const getModuleTreeCounts = (params: TableQueryParams) => alovaInstance.Post<Record<string, any>>(`${bugURL.getUnrelatedModuleTreeCountUrl}`, params);