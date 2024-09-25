import {ModuleTreeNode} from "/@/models/common.ts";
import {findNodePathByKey} from "/@/utils";
import {hasAnyPermission} from "/@/utils/permission.ts";
import {FormItem} from "/@/views/case-management/case-management-feature/components/types.ts";
import {CustomAttributes, TabItemType} from "/@/models/case-management/feature-case.ts";
import {useI18n} from "/@/hooks/use-i18n.ts";
import {StatusType} from "/@/enums/case-enum.ts";

const {t} = useI18n();
export const getModules = (moduleIds: string, treeData: ModuleTreeNode[]) => {
    const modules = findNodePathByKey(treeData, moduleIds, undefined, 'id');
    if (modules) {
        const moduleName = (modules || [])?.treePath.map((item: any) => item.name);
        if (moduleName.length === 1) {
            return moduleName[0];
        }
        return `/${moduleName.join('/')}`;
    }
};

export function initFormCreate(customFields: CustomAttributes[], permission: string[]) {
    return customFields.map((item: any) => {
        const multipleType = ['MULTIPLE_SELECT', 'CHECKBOX'];
        const numberType = ['INT', 'FLOAT'];
        const memberType = ['MEMBER', 'MULTIPLE_MEMBER'];
        const multipleInputType = ['MULTIPLE_INPUT'];
        const singleType = ['SELECT', 'RADIO'];
        let currentDefaultValue;
        const optionsValue = item.options || [];
        // 处理数字类型
        if (numberType.includes(item.type)) {
            currentDefaultValue = Number(item.defaultValue);
            // 处理多选项类型为空的默认值
        } else if (multipleType.includes(item.type) && Array.isArray(item.defaultValue) && item.defaultValue.length === 0) {
            currentDefaultValue = item.defaultValue;
            // 处理多选情况
        } else if (multipleType.includes(item.type)) {
            const tempValue = typeof item.defaultValue === 'string' ? JSON.parse(item.defaultValue) : item.defaultValue;
            const optionsIds = optionsValue.map((e: any) => e.value);
            currentDefaultValue = (optionsIds || []).filter((e: any) => tempValue.includes(e));
        } else if (memberType.includes(item.type)) {
            // @desc 上来为空成员且不包含默认值成员
            if (Array.isArray(item.defaultValue) && !item.defaultValue.includes('CREATE_USER')) {
                currentDefaultValue = item.type === 'MEMBER' ? '' : [];
                // @desc 包含默认值成员
            } else if (item.defaultValue.includes('CREATE_USER')) {
                currentDefaultValue = item.type === 'MEMBER' ? '' : [];
            } else {
                // @desc 如果默认原本的成员被系统移除则过滤掉该用户不展示
                const optionsIds = optionsValue.map((e: any) => e.value);
                if (item.type === 'MULTIPLE_MEMBER') {
                    const tempValue = typeof item.defaultValue === 'string' ? JSON.parse(item.defaultValue) : item.defaultValue;
                    currentDefaultValue = (optionsIds || []).filter((e: any) => tempValue.includes(e));
                } else {
                    currentDefaultValue = (optionsIds || []).find((e: any) => item.defaultValue === e) || '';
                }
            }
        } else if (multipleInputType.includes(item.type)) {
            currentDefaultValue = Array.isArray(item.defaultValue) ? item.defaultValue : JSON.parse(item.defaultValue);
        } else if (singleType.includes(item.type)) {
            const optionsIds = optionsValue.map((e: any) => e.value);
            currentDefaultValue = (optionsIds || []).find((e: any) => item.defaultValue === e) || '';
        } else {
            currentDefaultValue = item.defaultValue;
        }
        return {
            ...item,
            type: item.type,
            name: item.fieldId,
            label: item.fieldName,
            value: currentDefaultValue,
            required: item.required,
            options: optionsValue || [],
            props: {
                modelValue: currentDefaultValue,
                disabled: !hasAnyPermission(permission),
                options: optionsValue || [],
            },
        };
    }) as FormItem[];
}

export const statusIconMap: Record<string, any> = {
    UN_REVIEWED: {
        key: 'UN_REVIEWED',
        icon: StatusType.UN_REVIEWED,
        statusText: t('caseManagement.featureCase.notReviewed'),
        color: 'text-[var(--color-text-brand)]',
    },
    UNDER_REVIEWED: {
        key: 'UNDER_REVIEWED',
        icon: StatusType.UNDER_REVIEWED,
        statusText: t('caseManagement.featureCase.reviewing'),
        color: 'text-[rgb(var(--link-6))]',
    },
    PASS: {
        key: 'PASS',
        icon: StatusType.PASS,
        statusText: t('caseManagement.featureCase.passed'),
        color: '',
    },
    UN_PASS: {
        key: 'UN_PASS',
        icon: StatusType.UN_PASS,
        statusText: t('caseManagement.featureCase.notPass'),
        color: '',
    },
    RE_REVIEWED: {
        key: 'RE_REVIEWED',
        icon: StatusType.RE_REVIEWED,
        statusText: t('caseManagement.featureCase.retrial'),
        color: 'text-[rgb(var(--warning-6))]',
    },
};
export type CaseLevel = 'P0' | 'P1' | 'P2' | 'P3';
export const getCaseLevels = (customFields: CustomAttributes[]): CaseLevel => {
    const caseLevelItem = (customFields || []).find(
        (it: any) => it.internal && it.internalFieldKey === 'functional_priority'
    );
    return caseLevelItem?.options.find((it: any) => it.value === caseLevelItem.defaultValue)?.text as CaseLevel;
}

export const tabDefaultSettingList: TabItemType[] = [
    {
        value: 'basicInfo',
        label: t('caseManagement.featureCase.basicInfo'),
        canHide: false,
        isShow: true,
    },
    {
        value: 'detail',
        label: t('caseManagement.featureCase.detail'),
        canHide: false,
        isShow: true,
    },

    {
        value: 'case',
        label: t('caseManagement.featureCase.case'),
        canHide: true,
        isShow: true,
    },
 ]

export const caseTab: TabItemType[] = [
    {
        value: 'dependency',
        label: t('caseManagement.featureCase.dependency'),
        canHide: true,
        isShow: true,
    },
    {
        value: 'caseReview',
        label: t('caseManagement.featureCase.caseReview'),
        canHide: true,
        isShow: true,
    },
    {
        value: 'testPlan',
        label: t('caseManagement.featureCase.testPlan'),
        canHide: true,
        isShow: true,
    },
    {
        value: 'comments',
        label: t('caseManagement.featureCase.comments'),
        canHide: true,
        isShow: true,
    },
    {
        value: 'changeHistory',
        label: t('caseManagement.featureCase.changeHistory'),
        canHide: true,
        isShow: true,
    },
];

export const buggerTab: TabItemType[] = [
    {
        value: 'requirement',
        label: t('caseManagement.featureCase.requirement'),
        canHide: true,
        isShow: true,
    },
    {
        value: 'bug',
        label: t('caseManagement.featureCase.bug'),
        canHide: true,
        isShow: true,
    },
];
export const executionResultMap: Record<string, any> = {
    PENDING: {
        key: 'PENDING',
        icon: StatusType.PENDING,
        statusText: t('common.unExecute'),
        color: 'text-[var(--color-text-brand)]',
    },
    SUCCESS: {
        key: 'SUCCESS',
        icon: StatusType.SUCCESS,
        statusText: t('common.success'),
        color: '',
    },
    BLOCKED: {
        key: 'BLOCKED',
        icon: StatusType.BLOCKED,
        statusText: t('common.block'),
        color: 'text-[var(--color-fill-p-3)]',
    },
    ERROR: {
        key: 'ERROR',
        icon: StatusType.ERROR,
        statusText: t('common.fail'),
        color: '',
    },
};