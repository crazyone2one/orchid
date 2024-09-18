import {computed, ref} from "vue";
import {
    addOrUpdateOrdField,
    addOrUpdateProjectField,
    deleteOrdField,
    deleteProjectField,
    getFieldList,
    getOrdFieldDetail,
    getProjectFieldDetail,
    getProjectFieldList,
} from "/@/api/modules/setting/template.ts";
import {TemplateCardEnum, TemplateIconEnum} from "/@/enums/template-enum.ts";
import {useI18n} from "/@/hooks/use-i18n.ts";
import useTemplateStore from "/@/store/modules/setting/template.ts";
import {fieldIconAndNameModal, FormItemType} from "/@/models/setting/template.ts";
import dayjs from "dayjs";

const {t} = useI18n();
const templateStore = useTemplateStore();
const organizationState = computed(() => templateStore.ordStatus);
const projectState = computed(() => templateStore.projectStatus);

export function getCardList(type: string): Record<string, any>[] {
    const dataList = ref([
        {
            id: 1001,
            key: "FUNCTIONAL",
            value: TemplateCardEnum.FUNCTIONAL,
            name: t("system.orgTemplate.caseTemplates"),
        },
        {
            id: 1002,
            key: 'API',
            value: TemplateCardEnum.API,
            name: t('system.orgTemplate.APITemplates'),
        },
        {
            id: 1003,
            key: 'UI',
            value: TemplateCardEnum.UI,
            name: t('system.orgTemplate.UITemplates'),
        },
        {
            id: 1004,
            key: 'TEST_PLAN',
            value: TemplateCardEnum.TEST_PLAN,
            name: t('system.orgTemplate.testPlanTemplates'),
        },
        {
            id: 1005,
            key: "BUG",
            value: TemplateCardEnum.BUG,
            name: t("system.orgTemplate.defectTemplates"),
        },
    ]);
    if (type === "organization") {
        return dataList.value.map((item) => {
            return {
                ...item,
                enable: organizationState.value[item.key],
            };
        });
    }

    return dataList.value.map((item) => {
        return {
            ...item,
            enable: projectState.value[item.key],
        };
    });
}

export const getFieldRequestApi = (mode: "organization" | "project") => {
    if (mode === "organization") {
        return {
            list: getFieldList,
            delete: deleteOrdField,
            addOrUpdate: addOrUpdateOrdField,
            detail: getOrdFieldDetail,
        };
    }
    return {
        list: getProjectFieldList,
        delete: deleteProjectField,
        addOrUpdate: addOrUpdateProjectField,
        detail: getProjectFieldDetail,
    };
};
// 字段类型- 数字
export const numberTypeOptions: { label: string; value: FormItemType }[] = [
    {
        label: '整数',
        value: 'INT',
    },
    {
        label: '保留小数',
        value: 'FLOAT',
    },
];
export // table名称展示图标类型表格展示类型
const fieldIconAndName: fieldIconAndNameModal[] = [
    {
        value: 'INPUT',
        iconName: TemplateIconEnum.INPUT,
        label: t('system.orgTemplate.input'),
    },
    {
        value: 'TEXTAREA',
        iconName: TemplateIconEnum.TEXTAREA,
        label: t('system.orgTemplate.textarea'),
    },
    {
        value: 'SELECT',
        iconName: TemplateIconEnum.SELECT,
        label: t('system.orgTemplate.select'),
    },
    {
        value: 'MULTIPLE_SELECT',
        iconName: TemplateIconEnum.MULTIPLE_SELECT,
        label: t('system.orgTemplate.multipleSelect'),
    },
    {
        value: 'RADIO',
        iconName: TemplateIconEnum.RADIO,
        label: t('system.orgTemplate.radio'),
    },
    {
        value: 'CHECKBOX',
        iconName: TemplateIconEnum.CHECKBOX,
        label: t('system.orgTemplate.checkbox'),
    },
    {
        value: 'MEMBER',
        iconName: TemplateIconEnum.MEMBER,
        label: t('system.orgTemplate.member'),
    },
    {
        value: 'MULTIPLE_MEMBER',
        iconName: TemplateIconEnum.MULTIPLE_MEMBER,
        label: t('system.orgTemplate.multipleMember'),
    },
    {
        value: 'DATE',
        iconName: TemplateIconEnum.DATE,
        label: t('system.orgTemplate.date'),
    },
    {
        value: 'DATETIME',
        iconName: TemplateIconEnum.DATETIME,
        label: t('system.orgTemplate.dateTime'),
    },
    {
        value: 'NUMBER',
        iconName: TemplateIconEnum.NUMBER,
        label: t('system.orgTemplate.number'),
    },
    {
        value: 'INT',
        iconName: TemplateIconEnum.INT,
        label: t('system.orgTemplate.number'),
    },
    {
        value: 'FLOAT',
        iconName: TemplateIconEnum.FLOAT,
        label: t('system.orgTemplate.number'),
    },
    {
        value: 'MULTIPLE_INPUT',
        iconName: TemplateIconEnum.MULTIPLE_INPUT,
        label: t('system.orgTemplate.multipleInput'),
    },
    {
        value: 'SYSTEM',
        iconName: TemplateIconEnum.SYSTEM,
        label: '',
    },
];
// 字段类型-日期
export const dateOptions: { label: string; value: FormItemType }[] = [
    {
        label: dayjs().format('YYYY-MM-DD'),
        value: 'DATE',
    },
    {
        label: dayjs().format('YYYY-MM-DD HH:mm:ss'),
        value: 'DATETIME',
    },
];

export function getTemplateName(type: string, scene: string) {
    const dataList = getCardList(type);
    return dataList.find((item) => item.key === scene)?.name;
}