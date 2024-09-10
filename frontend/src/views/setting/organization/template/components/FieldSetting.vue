<script setup lang="ts">
import {useRequest} from "alova/client";
import {DataTableColumns, NAlert, NButton, NDivider, NSpace} from "naive-ui";
import {computed, h, onMounted, ref} from "vue";
import {useI18n} from "vue-i18n";
import {useRoute} from "vue-router";
import {getFieldRequestApi} from "./field-setting";
import {TableQueryParams} from "/@/models/common";
import {AddOrUpdateField, SceneType} from "/@/models/setting/template.ts";
import {useAppStore} from "/@/store";
import useTemplateStore from "/@/store/modules/setting/template.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";
import EditTemplateDrawer from '/@/views/setting/organization/template/components/EditFieldDrawer.vue'
import dayjs from "dayjs";
import PopConfirm from '/@/components/o-popconfirm/index.vue'
import {characterLimit} from "/@/utils";
import {UserListItem} from "/@/models/setting/user.ts";

const props = defineProps<{
  mode: "organization" | "project";
  deletePermission: string[];
  createPermission: string[];
  updatePermission: string[];
}>();
const appStore = useAppStore();
const templateStore = useTemplateStore();
const route = useRoute();
const {t} = useI18n();
const currentOrd = computed(() => appStore.currentOrgId);
const currentProjectId = computed(() => appStore.currentProjectId);
const reqParam = ref<TableQueryParams>({
  current: 1,
  pageSize: 10,
  keyword: "",
});
const templateType = computed(() => {
  switch (route.query.type) {
    case "API":
      return t("system.orgTemplate.templateApi");
    case "BUG":
      return t("system.orgTemplate.templateBug");
    default:
      return t("system.orgTemplate.templateCase");
  }
});
// 是否开启模板(项目/组织)
const isEnableTemplate = computed(() => {
  if (props.mode === "organization") {
    return templateStore.ordStatus[route.query.type as string];
  }
  return templateStore.projectStatus[route.query.type as string];
});

const hasOperationPermission = computed(() =>
    hasAnyPermission([...props.updatePermission, ...props.deletePermission])
);
const isEnabledTemplate = computed(() => {
  return props.mode === "organization"
      ? templateStore.projectStatus[scene.value as string]
      : !templateStore.projectStatus[scene.value as string];
});
const showDrawer = ref<boolean>(false);
const isShowTip = ref<boolean>(true);
const scene = ref<SceneType>(route.query.type);
const getParams = () => {
  scene.value = route.query.type;
  reqParam.value.scene = scene.value;
  reqParam.value.scopedId =
      props.mode === "organization" ? currentOrd.value : currentProjectId.value;
  return reqParam.value;
};
const getList = getFieldRequestApi(props.mode).list;
const {send, data} = useRequest(() => getList(reqParam.value), {
  immediate: false,force:true
});
const columns: DataTableColumns<AddOrUpdateField> = [
  {
    type: 'selection'
  },
  {
    title: t('system.orgTemplate.name'),
    key: 'name',
    width: 300
  },
  {
    title: t('system.orgTemplate.columnFieldType'),
    key: 'type',
  },
  {
    title: t('system.orgTemplate.columnFieldDescription'),
    key: 'remark',
    width: 300
  },
  {
    title: t('system.orgTemplate.columnFieldUpdatedTime'),
    key: 'updateTime',
    render: (record) => {
      return h('span', {}, {default: () => dayjs(record.updateTime).format('YYYY-MM-DD HH:mm:ss')})
    }
  },
  {
    title: hasOperationPermission.value ? t('system.orgTemplate.operation') : '',
    key: 'operation',
    fixed: 'right',
    width: hasOperationPermission.value ? 200 : 50,
    render: (record) => {
      return h(NSpace, {}, {
        default: () => {
          return [
            h(PopConfirm, {
              title: t('system.orgTemplate.updateTip', {name: characterLimit(record.name)}),
              subTitleTip: t('system.orgTemplate.updateDescription', {type: templateType.value})
            }, {
              trigger: () => h(NButton, {text: true, type: 'primary'}, {default: () => t('system.orgTemplate.edit')})
            }),
            h(NDivider, {vertical: true, class: 'h-[12px]'}, {}),
            h(NButton, {
              text: true,
              type: 'error',
              onClick: () => handlerDelete(record)
            }, {default: () => t('system.userGroup.delete')})
          ]
        }
      })
    }
  },
];
const deleteApi = getFieldRequestApi(props.mode).delete;
const {send: deleteCustomField} = useRequest((id) => deleteApi(id), {immediate: false})
const handlerDelete = (record: AddOrUpdateField) => {
  let contentStr = t('system.orgTemplate.deleteFiledContentNotUsed');
  if (record.used) {
    contentStr = t('system.orgTemplate.deleteFiledContent');
  }
  window.$dialog.error({
    title: t('system.orgTemplate.deleteTitle', {name: characterLimit(record.name)}),
    content: contentStr,
    positiveText: t('common.confirmDelete'),
    negativeText: t('common.cancel'),
    onPositiveClick: () => {
      try {
        if (record.id) deleteCustomField(record.id).then(() => {
          window.$message.success(t('system.orgTemplate.deleteSuccess'));
          fetchData();
        })
      } catch (error) {
        console.log(error);
      }
    }
  })
}
const successHandler = () => {
  fetchData();
};
const fetchData = () => {
  getParams();
  send();
};
const handleAdd = () => {
  showDrawer.value = true;
}
onMounted(() => {
  fetchData();
});
</script>

<template>
  <div>
    <n-alert
        v-if="isShowTip &&hasAnyPermission([...props.createPermission, ...props.updatePermission])"
        class="mb-6"
        :type="
        isEnabledTemplate && props.mode === 'organization' ? 'warning' : 'info'
      "
    >
      <div class="flex items-start justify-between">
        <span>
          {{
            isEnabledTemplate && props.mode === "organization"
                ? t("system.orgTemplate.enableDescription")
                : t("system.orgTemplate.fieldLimit")
          }}</span
        >
        <!--        <span class="cursor-pointer" @click="noRemindHandler">-->
        <!--          {{ t("system.orgTemplate.noReminders") }}-->
        <!--        </span>-->
      </div>
    </n-alert>
    <div class="mb-4 flex items-center justify-between">
      <span v-if="isEnabledTemplate" class="font-medium">
        {{ t("system.orgTemplate.fieldList") }}
      </span>
      <n-button type="primary" @click="handleAdd">
        {{ t("system.orgTemplate.addField") }}
      </n-button>
      <div>
        <n-input
            :placeholder="t('system.orgTemplate.searchTip')"
            clearable
            class="w-[230px]"
        />
      </div>
    </div>
    <n-data-table :columns="columns" :data="data" :row-key="(tmp:AddOrUpdateField)=>tmp.id"/>
  </div>
  <edit-template-drawer v-model:visible="showDrawer" :mode="props.mode" :data="[]" @success="successHandler"/>
</template>

<style scoped></style>
