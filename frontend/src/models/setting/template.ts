import {LocationQueryValue} from "vue-router";

export type FormItemType =
    | 'INPUT'
    | 'TEXTAREA'
    | 'SELECT'
    | 'MULTIPLE_SELECT'
    | 'RADIO'
    | 'CHECKBOX'
    | 'MEMBER'
    | 'MULTIPLE_MEMBER'
    | 'DATE'
    | 'DATETIME'
    | 'INT'
    | 'FLOAT'
    | 'MULTIPLE_INPUT'
    | 'INT'
    | 'FLOAT'
    | 'NUMBER'
    | 'PassWord'
    | 'CASCADER'
    | undefined;
export type SceneType = 'FUNCTIONAL' | 'BUG' | 'API' | 'UI' | 'TEST_PLAN' | LocationQueryValue[] | LocationQueryValue;

export interface FieldOptions {
    fieldId?: string;
    value: any;
    text: string;
    internal?: boolean; // 是否是内置模板
    pos: number; // 排序字段
}

export interface AddOrUpdateField {
    id?: string;
    name: string;
    used: boolean;
    scene: SceneType; // 使用场景
    type: FormItemType;
    remark: string; // 备注
    scopeId: string; // 组织或项目ID
    options?: FieldOptions[];
    enableOptionKey: boolean;

    [key: string]: any;
}
// 自定义字段
export interface DefinedFieldItem {
    id: string;
    name: string;
    scene: SceneType; // 使用场景
    type: FormItemType; // 表单类型
    remark: string;
    internal: boolean; // 是否是内置字段
    scopeType: string; // 组织或项目级别字段（PROJECT, ORGANIZATION）
    createTime: number;
    updateTime: number;
    createUser: string;
    refId: string | null; // 项目字段所关联的组织字段ID
    enableOptionKey: boolean | null; // 是否需要手动输入选项key
    scopeId: string; // 组织或项目ID
    options: FieldOptions[] | null;
    required?: boolean | undefined;
    fApi?: any; // 表单值
    // formRules?: FormRuleItem[] | FormItem[] | FormRule[]; // 表单列表
    [key: string]: any;
}
export interface fieldIconAndNameModal {
    value: string;
    iconName: string; // 图标名称
    label: string; // 对应标签
}
export interface CustomField {
    fieldId: string;
    required?: boolean; // 是否必填
    apiFieldId?: string; // api字段名
    defaultValue?: string | string[] | null | number; // 默认值
    [key: string]: any;
}
export interface DetailCustomField extends CustomField {
    fieldId: string;
    fieldName: string;
    fieldKey: string;
    required: boolean;
    type: string;
    internal: boolean;
    internalFieldKey: string; // 系统字段标识 例如用例等级
    options: FieldOptions[];
    supportSearch?: boolean;
    optionMethod?: string;
    platformOptionJson?: string; // 三方平台下拉选项
    platformPlaceHolder?: string;
    platformSystemField?: any; // 三方平台字段
    apiFieldId?: string; // 三方api
}