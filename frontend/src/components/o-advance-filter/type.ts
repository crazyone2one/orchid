import {OperatorEnum, FilterType} from "/@/enums/advanced-filter-enum.ts";
import type {CascaderOption, CascaderProps, SelectProps, TreeSelectProps} from "naive-ui";
import {TreeNodeData} from "/@/components/o-tree/types.ts";

export enum BackEndEnum {
    STRING = 'string',
    ARRAY = 'array',
    TIME = 'time',
    NUMBER = 'number',
}

export interface NumberProps {
    mode: 'embed' | 'button';
    precision: number;
    step: number;
}

export interface FilterFormItem {
    dataIndex?: string; // 第一列下拉的value
    title?: string; // 第一列下拉显示的label
    operator?: OperatorEnum; // 第二列的值
    type: FilterType; // 类型：判断第二列下拉数据和第三列显示形式
    value?: any; // 第三列的值
    customField?: boolean; // 是否是自定义字段
    customFieldType?: string; // 自定义字段的类型
    cascaderOptions?: CascaderOption[]; // 级联选择的选项
    numberProps?: Partial<NumberProps>;
    selectProps?: Partial<SelectProps>; // select的props, 参考 MsSelect
    cascaderProps?: Partial<CascaderProps>; // cascader的props, 参考 MsCascader
    treeSelectData?: TreeNodeData[];
    treeSelectProps?: Partial<TreeSelectProps>;
}

export type AccordBelowType = 'AND' | 'OR';

export type CombineItem = Pick<FilterFormItem, 'value' | 'operator' | 'customField'>;

export interface ConditionsItem extends CombineItem {
    name?: string;
}

export interface FilterResult {
    // 匹配模式 所有/任一
    searchMode?: AccordBelowType;
    // 高级搜索
    conditions?: ConditionsItem[];
    combine?: any;
}

export interface ViewItem {
    id: string;
    userId: string;
    name: string;
    viewType: string; // 视图类型，例如功能用例视图
    internal: boolean; // 是否为内置视图
    scopeId: string; // 视图的应用范围，一般为项目ID
    searchMode: string;
    pos?: number; // 自定义排序
    createTime?: number;
    updateTime?: number;
    isShowNameInput?: boolean;
}

export interface ViewList {
    internalViews: ViewItem[];
    customViews: ViewItem[];
}

export interface ViewParams extends FilterResult {
    id?: string;
    name: string;
    scopeId?: string;
}

export interface ViewDetail extends ViewParams {
    userId?: string;
    viewType?: string;
    internal?: boolean; // 是否为内置视图
    createTime?: number;
    updateTime?: number;
}

export interface FilterForm  {
    list: FilterFormItem[];
    // 匹配模式 所有/任一
    searchMode?: AccordBelowType;
    // 高级搜索
    conditions?: ConditionsItem[];
    combine?: any;
    id?: string;
    name: string;
    scopeId?: string;
    userId?: string;
    viewType?: string;
    internal?: boolean; // 是否为内置视图
    createTime?: number;
    updateTime?: number;
}