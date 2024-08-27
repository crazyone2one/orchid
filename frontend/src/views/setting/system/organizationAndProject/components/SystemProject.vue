<script setup lang="ts">
import {computed, h, onMounted, ref, resolveDirective, withDirectives} from "vue";
import {DataTableColumns, NSwitch} from "naive-ui";
import {useI18n} from "vue-i18n";
import {usePagination} from "alova/client";
import {enableOrDisableOrg, postProjectTable} from "/@/api/modules/setting/system-org-project.ts";
import {TableQueryParams} from "/@/models/common.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";
import {OrgProjectTableItem} from "/@/models/orgAndProject.ts";
import ShowOrEdit from '/@/components/show-or-edit/index.vue'
import OButton from '/@/components/o-button/index.vue'

const {t} = useI18n()
const permission = resolveDirective('permission')
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
  if (hasAnyPermission(['PROJECT_BASE_INFO:READ'])) {
    return 100;
  }
  return 50;
});
const handleRename = (value: string) => {
  window.$message.info('todo...')
  // todo 调用接口
}
const columns: DataTableColumns<OrgProjectTableItem> = [
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
    ellipsis: {tooltip: true},
    render(row) {
      return h(ShowOrEdit, {value: row.name, onUpdateValue: (v: string) => handleRename(v)})
    }
  },
  {
    title: t('system.organization.member'),
    key: 'memberCount'
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
    title: t('system.organization.subordinateOrg'),
    key: 'organizationName',
    ellipsis: {tooltip: true},
    width: 200
  },
  {
    title: t('system.organization.operation'),
    key: 'operation',
    fixed: 'right',
    width: operationWidth.value,
    render(row) {
      if (!row.enable) {
        return h(OButton, {text: true, content: t('common.revokeDelete')}, {});
      } else {
        const result = []
        result.push(
            withDirectives(h(OButton, {
              text: true,
              content: t('common.edit')
            }, {}), [[permission, ['SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE']]])
        )
        if (hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+ADD_MEMBER1'])) {
          result.push(
              h(OButton, {text: true, content: t('system.organization.addMember')}, {}),
          )
        }
        return result;
      }
    }
  },
]
const handleEnableOrDisableOrg = (record: OrgProjectTableItem, value: boolean) => {
  window.$dialog.info({
    title: value ? t('system.project.enableTitle') : t('system.project.endTitle'),
    content: value ? t('system.project.enableContent') : t('system.project.endContent'),
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
const {send: fetchData, data} = usePagination(() => postProjectTable(reqParam.value), {
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
  <n-data-table :bordered="false" :columns="columns" :data="data" :row-key="(tmp:OrgProjectTableItem)=>tmp.id"/>
</template>

<style scoped>

</style>