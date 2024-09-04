import type {TreeOption} from 'naive-ui'

export type TreeNodeData = {
    hideMoreAction?: boolean; // 隐藏更多操作
    parentId?: string;
    expanded?: boolean; // 是否展开
    [key: string]: any;
} & TreeOption;

export interface TreeFieldNames {
    key: string;
    title: string;
    children: string;

    [key: string]: any;
}