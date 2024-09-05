export enum FilterType {
    INPUT = 'Input',
    NUMBER = 'Number',
    SELECT = 'Select',
    DATE_PICKER = 'DatePicker',
    CASCADER = 'Cascader',
    TAGS_INPUT = 'TagsInput',
    TREE_SELECT = 'TreeSelect',
    TEXTAREA = 'textArea',
    RADIO = 'radio',
    CHECKBOX = 'checkbox',
    JIRAKEY = 'JIRAKEY',
}
export enum BackEndEnum {
    STRING = 'string',
    ARRAY = 'array',
    TIME = 'time',
    NUMBER = 'number',
}
export interface FilterFormItem {
    dataIndex?: string; // 对应的row的数据key
    title?: string; // 显示的label 国际化字符串定义在前端
    type: FilterType; // 类型：Input,Select,DatePicker,RangePicker
    value?: any; // 值 字符串 和 数组
    operator?: string; // 运算符号
    backendType?: BackEndEnum; // 后端类型 string array time
}

export type AccordBelowType = 'AND' | 'OR';

export interface CombineItem {
    [key: string]: Pick<FilterFormItem, 'value' | 'operator' | 'backendType'>;
}

export interface FilterResult {
    // 匹配模式 所有/任一
    accordBelow: AccordBelowType;
    // 高级搜索
    combine: CombineItem;
}