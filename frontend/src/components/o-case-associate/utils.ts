import {StatusType} from "/@/enums/case-enum.ts";

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
