<script setup lang="ts">
import {computed, h, onMounted, ref, resolveDirective, withDirectives} from "vue";
import {type DataTableColumns, NAlert, NSwitch} from "naive-ui";
import {useI18n} from "vue-i18n";
import {usePagination} from "alova/client";
import {
  deleteProject,
  enableOrDisableProject,
  postProjectTable,
  revokeDeleteProject
} from "/@/api/modules/setting/system-org-project.ts";
import {TableQueryParams} from "/@/models/common.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";
import {CreateOrUpdateSystemProjectParams, OrgProjectTableItem} from "/@/models/orgAndProject.ts";
import ShowOrEdit from '/@/components/show-or-edit/index.vue'
import OButton from '/@/components/o-button/index.vue'
import AddProjectModal from "/@/views/setting/system/organizationAndProject/components/AddProjectModal.vue";
import {UserItem} from "/@/models/setting/log.ts";
import Pagination from '/@/components/o-pagination/index.vue'
import AddUserModal from "/@/views/setting/system/organizationAndProject/components/AddUserModal.vue";
import {characterLimit} from "/@/utils";

const {t} = useI18n()
const permission = resolveDirective('permission')
const addProjectModalRef = ref<InstanceType<typeof AddProjectModal> | null>(null)
const addProjectVisible = ref(false);
const currentUpdateProject = ref<CreateOrUpdateSystemProjectParams>();
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
        return withDirectives(h(OButton, {
              text: true, content: t('common.delete'),
              onClick: () => handleDelete(row)
            }, {}),
            [[permission, ['SYSTEM_ORGANIZATION_PROJECT:READ+DELETE']]]);
      } else {
        const result = []
        result.push(
            withDirectives(h(OButton, {
              text: true,
              content: t('common.edit'),
              onClick: () => showAddProjectModal(row)
            }, {}), [[permission, ['SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE']]])
        )
        if (hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+ADD_MEMBER'])) {
          result.push(
              h(OButton, {
                text: true, content: t('system.organization.addMember'),
                onClick: () => showAddUserModal(row)
              }, {}),
          )
        }
        if (hasAnyPermission(['PROJECT_BASE_INFO:READ'])) {
          result.push(
              h(OButton, {text: true, content: t('system.project.enterProject')}, {}),
          )
        }
        return result;
      }
    }
  },
]
const handleDelete = (record: OrgProjectTableItem) => {
  window.$dialog.error({
    title: t('system.project.deleteName', {name: characterLimit(record.name)}),
    content: t('system.project.deleteTip'),
    positiveText: t('common.confirmDelete'),
    negativeText: t('common.cancel'),
    onPositiveClick: async () => {
      await deleteProject(record.id);
      window.$message.success(() => t('common.deleteSuccess'), {
        render: () => h(NAlert,
            {
              class: 'ml-[8px] cursor-pointer text-[rgb(64,128,255)]',
              title: t('common.deleteSuccess'),
              type: 'success',
              style: {
                boxShadow: 'var(--n-box-shadow)',
                maxWidth: 'calc(100vw - 32px)',
                width: '480px'
              }
            },

            {
              default: () => withDirectives(h('span',
                  {
                    class: 'ml-[8px] cursor-pointer text-[rgb(64,128,255)]',
                    onClick: () => handleRevokeDelete(record)
                  },
                  {default: () => t('common.revoke'),}
              ), [[permission, ['SYSTEM_ORGANIZATION_PROJECT:READ+RECOVER']]]),
            }
        )
      })
      await fetchData()
    }
  })
}
const handleRevokeDelete = (record: OrgProjectTableItem) => {
  window.$dialog.error({
    title: t('system.project.revokeDeleteTitle', {name: characterLimit(record.name)}),
    positiveText: t('common.revokeDelete'),
    negativeText: t('common.cancel'),
    onPositiveClick: async () => {
      await revokeDeleteProject(record.id);
      window.$message.success(t('common.revokeDeleteSuccess'));
      await fetchData();
    }
  })
}
const handleEnableOrDisableOrg = (record: OrgProjectTableItem, value: boolean) => {
  window.$dialog.info({
    title: value ? t('system.project.enableTitle') : t('system.project.endTitle'),
    content: value ? t('system.project.enableContent') : t('system.project.endContent'),
    positiveText: value ? t('common.confirmStart') : t('common.confirmEnd'),
    negativeText: t('common.cancel'),
    onPositiveClick: async () => {
      try {
        await enableOrDisableProject(record.id, value);
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
  pageSize: 10,
  keyword: ''
})
const {send: fetchData, data, page, pageSize, total} = usePagination((page, pageSize) => {
  reqParam.value.current = page
  reqParam.value.pageSize = pageSize
  return postProjectTable(reqParam.value)
}, {
  immediate: false,// 请求前的初始数据（接口返回的数据格式）
  initialData: {
    total: 0,
    data: []
  },
  data: response => response.records,
  total: response => response.totalRow
})
const handleSetPage = (param: number) => page.value = param
const handleSetPageSize = (param: number) => pageSize.value = param
const showAddProjectModal = (record: OrgProjectTableItem) => {
  const {id, name, description, enable, adminList, organizationId, moduleIds, resourcePoolList} = record;
  addProjectVisible.value = true;
  currentUpdateProject.value = {
    id,
    name,
    description,
    enable,
    userIds: adminList.map((item: UserItem) => item.id),
    organizationId,
    moduleIds,
    resourcePoolIds: resourcePoolList.map((item: { id: string }) => item.id),
  };
};
const handleCancel = (shouldSearch: boolean) => {
  if (shouldSearch) {
    fetchData();
  }
  addProjectVisible.value = false;
};
const currentProjectId = ref('');
const userVisible = ref(false);
const showAddUserModal = (record: OrgProjectTableItem) => {
  currentProjectId.value = record.id;
  userVisible.value = true;
};
const handleAddUserModalCancel = () => {
  userVisible.value = false;
};
defineExpose({fetchData})
onMounted(() => {
  fetchData()
})
</script>

<template>
  <div>
    <n-data-table :bordered="false" :columns="columns" :data="data" :row-key="(tmp:OrgProjectTableItem)=>tmp.id"/>
    <div class="mt-8">
      <pagination :page-size="pageSize" :page="page" :count="total"
                  @update-page="handleSetPage"
                  @update-page-size="handleSetPageSize"/>
    </div>
  </div>
  <add-project-modal ref="addProjectModalRef"
                     :visible="addProjectVisible"
                     :current-project="currentUpdateProject"
                     @cancel="handleCancel"/>
  <add-user-modal :project-id="currentProjectId"
                  :visible="userVisible"
                  @submit="fetchData"
                  @cancel="handleAddUserModalCancel"/>
</template>

<style scoped>

</style>