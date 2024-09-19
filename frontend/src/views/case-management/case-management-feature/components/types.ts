import {FormRules} from "naive-ui";

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
export interface FormItemDefaultOptions {
    text: string;
    value: string;
}
export interface FormItem {
    type: FormItemType;
    name: string; // 表单项名称，作为唯一标志 --field
    label: string; // 表单项文本 --- title
    // 选择器的值绑定为Record<string, any>，避免携带远程搜索时默认选中的选项不在 options 列表中，所以还需要设置 fallbackOptions
    value: string | number | boolean | string[] | number[] | Record<string, any> | Record<string, any>[];
    subDesc?: string; // 表单项描述，在表单项下方显示
    inputSearch?: boolean; // 是否支持远程搜索
    tooltip?: string; // 表单后边的提示info
    instructionsIcon?: ''; // 是否有图片在表单后边展示
    platformPlaceHolder?: string; // 平台表单项占位符
    optionMethod?: string; // 请求检索的方法 两个参数 表单项的所有值
    options?: FormItemDefaultOptions[];
    required: boolean;
    validate?: FormRules;
    control?: {
        value: string;
        rule: FormItem[];
    }[];
    // 表单联动配置
    couplingConfig?: {
        // 联动类型，visible：显示隐藏，disabled：禁用启用，filterOptions：过滤选项，disabledOptions：禁用选项，initOptions：初始化选项。都由联动的表单项触发
        type?: 'initOptions'; // 目前初始化选项
        cascade?: string; // 联动表单项名称
        matchRule?: 'same' | 'includes' | 'excludes' | RegExp; // 联动匹配规则，same：值相同，includes：值包含，excludes：值不包含， RegExp：自定义匹配正则表达式 // 场景 目前只考虑等于情况
        [key: string]: any;
    };
    // 表单控制器
    displayConditions?: {
        field: string;
        value: any;
    };
    // 表单布局
    wrap?: Record<string, any>;
    props?: Record<string, any>;
    [key: string]: any;
}
export interface FormRuleItem {
    type: string; // 表单类型
    field: string; // 字段
    title: string; // label 表单标签
    value: string | string[] | number | number[]; // 目前的值
    effect: {
        required: boolean; // 是否必填
    };
    // 级联关联到某一个form上 可能存在多个级联
    options: FormItemDefaultOptions[];
    link: string[];
    // 梳理表单所需要属性
    control: { value: string; rule: FormRuleItem[] };
    props: Record<string, any>;
    [key: string]: any;
}