<script setup lang="ts">
import {computed, h, onMounted, ref} from "vue";
import {OrganizationListItem} from "/@/models/organization.ts";
import type {DataTableColumns} from "naive-ui";
import {NSwitch} from "naive-ui";
import {useI18n} from "vue-i18n";
import {usePagination} from "alova/client";
import {enableOrDisableOrg, postOrgTable} from "/@/api/modules/setting/system-org-project.ts";
import {TableQueryParams} from "/@/models/common.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";

const {t} = useI18n()

const hasOperationPermission = computed(() =>
    hasAnyPermission([
      'SYSTEM_ORGANIZATION_PROJECT:READ+RECOVER',
      'SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE',
      'SYSTEM_ORGANIZATION_PROJECT:READ+DELETE',
    ])
);
const operationWidth = computed(() => {
  if (hasOperationPermission.value) {
    return 250;
  }
  if (hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ'])) {
    return 100;
  }
});
const columns: DataTableColumns<OrganizationListItem> = [
  {
    type: 'selection'
  },
  {
    title: t('system.organization.ID'),
    width: 100,
    key: 'num'
  },
  {
    title: t('system.organization.name'),
    key: 'name',
    width: 300,
    ellipsis: {tooltip: true}
  },
  {
    title: t('system.organization.member'),
    key: 'memberCount'
  },
  {
    title: t('system.organization.project'),
    key: 'projectCount'
  },
  {
    title: t('system.organization.status'),
    key: 'enable',
    render(row) {
      return h(NSwitch, {
        value: row.enable,
        disabled: !hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE']),
        size: 'small',
        "onUpdate:value": (value) => handleEnableOrDisableOrg(row, value)
      }, {})
    }
  },
  {
    title: t('common.desc'),
    key: 'description',
    ellipsis: {tooltip: true}
  },
  {
    title: t('system.organization.operation'),
    key: 'operation',
    fixed: 'right',
    width: operationWidth.value
  },
]
const handleEnableOrDisableOrg = (record: OrganizationListItem, value: boolean) => {
  window.$dialog.info({
    title: value ? t('system.organization.enableTitle') : t('system.organization.endTitle'),
    content: value ? t('system.organization.enableContent') : t('system.organization.endContent'),
    positiveText: value ? t('common.confirmStart') : t('common.confirmEnd'),
    negativeText: t('common.cancel'),
    onPositiveClick: async () => {
      try {
        await enableOrDisableOrg(record.id, value);
        window.$message.success(value ? t('common.enableSuccess') : t('common.closeSuccess'))
        await fetchData()
      } catch (e) {
        console.log(e)
      }
    }
  })
}
// const data = ref([])
const reqParam = ref<TableQueryParams>({
  current: 1,
  pageSize: 6,
  keyword: ''
})
const {send: fetchData, data} = usePagination(() => postOrgTable(reqParam.value), {
  immediate: false,// 请求前的初始数据（接口返回的数据格式）
  initialData: {
    total: 0,
    data: []
  },
  data: response => response.records,
  total: response => response.total
})

defineExpose({fetchData})
onMounted(() => {
  fetchData()
})
</script>

<template>
  <n-data-table :bordered="false" :columns="columns" :data="data" :row-key="(tmp:OrganizationListItem)=>tmp.id"/>
</template>

<style scoped>

</style>