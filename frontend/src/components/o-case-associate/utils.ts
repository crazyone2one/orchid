import {StatusType} from "/@/enums/case-enum.ts";
import {getCaseModulesCounts, getPublicLinkCaseModulesCounts} from "/@/api/modules/case-management/feature-case.ts";
import {getModuleTreeCounts} from "/@/api/modules/bug-management";

export enum RequestModuleEnum {
    API_CASE = 'API_CASE',
    CASE_MANAGEMENT = 'CASE_MANAGEMENT',
    BUG_MANAGEMENT = 'BUG_MANAGEMENT',
}

export const lastExecuteResultMap: Record<string, any> = {
    PENDING: {
        label: 'PENDING',
        icon: StatusType.PENDING,
        statusText: 'common.unExecute',
        color: 'rgb(29,33,41)',
    },
    SUCCESS: {
        label: 'SUCCESS',
        icon: StatusType.SUCCESS,
        statusText: 'common.success',
        color: '',
    },
    BLOCKED: {
        label: 'BLOCKED',
        icon: StatusType.BLOCKED,
        statusText: 'common.block',
        color: 'var(--color-fill-p-3)',
    },
    ERROR: {
        label: 'ERROR',
        icon: StatusType.ERROR,
        statusText: 'common.fail',
        color: '',
    },
};

export const initGetModuleCountFunc = (type: RequestModuleEnum[keyof RequestModuleEnum], params: Record<string, any>) => {
    switch (type) {
        case RequestModuleEnum.API_CASE:
            return getPublicLinkCaseModulesCounts(params);
        case RequestModuleEnum.CASE_MANAGEMENT:
            return getCaseModulesCounts(params);
        case RequestModuleEnum.BUG_MANAGEMENT:
            return getModuleTreeCounts(params);
        default:
            break;
    }
}