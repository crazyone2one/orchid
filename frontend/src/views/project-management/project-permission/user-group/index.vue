<script setup lang="ts">
import type {DataTableColumns, FormInst} from "naive-ui";
import {NButton, NDivider} from "naive-ui";
import {CurrentUserGroupItem, SystemUserGroupParams, UserGroupItem} from "/@/models/setting/user-group.ts";
import {useI18n} from "vue-i18n";
import {computed, h, onMounted, ref} from "vue";
import {TableQueryParams} from "/@/models/common.ts";
import {useForm, usePagination} from "alova/client";
import {postUserGroupList, updateOrAddProjectUserGroup} from "/@/api/modules/project-management/user-group.ts";
import {useAppStore} from "/@/store";
import Pagination from "/@/components/o-pagination/index.vue";
import {hasAnyPermission} from "/@/utils/permission.ts";
import {cloneDeep} from "lodash-es";
import {AuthScopeEnum} from "/@/enums/common-enum.ts";
import UserDrawer from "/@/views/project-management/project-permission/user-group/UserDrawer.vue";

const {t} = useI18n();
const appStore = useAppStore();
const currentProjectId = computed(() => appStore.currentProjectId);
const columns: DataTableColumns<UserGroupItem> = [
  {
    type: 'selection'
  },
  {
    title: t('project.userGroup.name'),
    key: 'name',
    ellipsis: {tooltip: true},
    render(row) {
      return h('div', {class: 'flex flex-row items-center gap-[4px]'}, {
        default: () => [
          h('span', {class: 'one-line-text'}, {default: () => row.name}),
          h('span', {class: 'ml-1 text-gray-500'}, {
            default: () => `(${row.internal ? t('common.internal') : row.scopeId === 'global' ? t('common.system.custom')
                : t('common.custom')})`
          }),
        ]
      })
    }
  },
  {
    title: t('project.userGroup.memberCount'),
    key: 'memberCount',
    render(row) {
      if (hasAnyPermission(['PROJECT_GROUP:READ+UPDATE'])) {
        return h(NButton, {
          text: true,
          onClick: () => showUserDrawer(row)
        }, {default: () => row.memberCount})
      }
      return h('span', {}, {default: () => row.memberCount})
    }
  },
  {
    title: t('common.operation'),
    key: 'operation',
    fixed: 'right',
    width: 150,
    render(row) {
      return h('div', {class: 'flex flex-row flex-nowrap'}, {
        default: () => {
          const tmp = [];
          if (hasAnyPermission(['PROJECT_GROUP:READ'])) {
            tmp.push(h('span', {class: 'flex flex-row'},
                {
                  default: () => {
                    const r = [
                      h(NButton, {class: '!mr-0', text: true, type: 'primary'},
                          {default: () => t('project.userGroup.viewAuth')}),
                    ]
                    if (row.scopeId !== 'global') {
                      r.push(h(NDivider, {vertical: true}, {}))
                    }
                    return r;
                  }
                }))
          }
          if (hasAnyPermission(['PROJECT_GROUP:READ+UPDATE']) && row.scopeId !== 'global') {
            tmp.push(h(NButton, {class: '!mr-0', text: true, type: 'error'}, {default: () => t('common.delete')}),);
          }
          return tmp;
        }
      })
    }
  },
];
const reqParam = ref<TableQueryParams>({
  current: 1,
  pageSize: 10,
  keyword: ''
})
const currentItem = ref<CurrentUserGroupItem>({
  id: '',
  name: '',
  type: AuthScopeEnum.PROJECT,
  internal: true,
  scopeId: '',
});
const {send: fetchData, data, page, pageSize, total} = usePagination((page, pageSize) => {
  reqParam.value.current = page
  reqParam.value.pageSize = pageSize
  return postUserGroupList(reqParam.value)
}, {
  immediate: false,
  initialData: {
    total: 0,
    data: []
  },
  data: response => response.records,
  total: response => response.total,
  // watchingStates: [reqParam]
})
const handleSetPage = (param: number) => page.value = param
const handleSetPageSize = (param: number) => pageSize.value = param;

const userVisible = ref(false);
const showUserDrawer = (record: UserGroupItem) => {
  const {id, internal, name, scopeId, type} = record;
  currentItem.value = {id, internal, name, scopeId, type};
  userVisible.value = true;
};
const addUserGroupFormRef = ref<FormInst | null>(null)
const addUserGroupVisible = ref(false);
const handleAddUserGroup = () => {
  addUserGroupVisible.value = true;

}
const handleAddUGCancel = (shouldSearch: boolean) => {
  addUserGroupFormRef.value?.restoreValidation();
  addUserGroupVisible.value = false;
  resetForm()
  if (shouldSearch) {
    fetchData();
  }
}
const {
  // 提交状态
  loading: addUserGroupLoading,
  // 响应式的表单数据，内容由initialForm决定
  form, send: submit, reset: resetForm
} = useForm(
    formData => {
      const param: SystemUserGroupParams = cloneDeep(formData) as SystemUserGroupParams;
      param.scopeId = appStore.currentProjectId
      return updateOrAddProjectUserGroup(param);
    },
    {
      // 初始化表单数据
      initialForm: {
        name: '',
      },
    }
);
const handleCreateUserGroup = () => {
  addUserGroupFormRef.value?.validate(errors => {
    if (errors) {
      return
    }
    submit().then(() => {
      window.$message.success(t('common.createSuccess'));
      handleAddUGCancel(true);
    })
  })
}
onMounted(() => {
  reqParam.value.projectId = currentProjectId.value
  fetchData()
})
</script>

<template>
  <div class="flex flex-row items-center justify-between mb-5">
    <n-button v-permission="['PROJECT_GROUP:READ+ADD']" type="primary" @click="handleAddUserGroup">
      {{ $t('project.userGroup.add') }}
    </n-button>
    <div>
      <n-input v-model:value="reqParam.keyword" :placeholder="$t('project.userGroup.searchUser')"
               class="w-[240px]" clearable @clear="fetchData"
               @keyup.enter.prevent="fetchData"/>
    </div>
  </div>
  <n-data-table :bordered="false" :columns="columns" :data="data" :row-key="(tmp:UserGroupItem)=>tmp.id"/>
  <div class="mt-8">
    <pagination :page-size="pageSize" :page="page" :count="total"
                @update-page="handleSetPage"
                @update-page-size="handleSetPageSize"/>
  </div>
  <n-modal v-model:show="addUserGroupVisible" preset="dialog" :mask-closable="false" @close="handleAddUGCancel(false)">
    <template #header>
      {{ t('project.userGroup.addUserGroup') }}
    </template>
    <div>
      <n-form ref="addUserGroupFormRef" :model="form">
        <n-form-item :label="$t('project.userGroup.name')" path="name"
                     :rule="[
            { required: true, message: t('project.userGroup.addRequired') },
            { maxLength: 255, message: t('common.nameIsTooLang') },
          ]">
          <n-input v-model:value="form.name" clearable/>
        </n-form-item>
      </n-form>
    </div>
    <template #action>
      <n-button secondary :loading="addUserGroupLoading" @click="handleAddUGCancel(false)">
        {{ t('common.cancel') }}
      </n-button>
      <n-button type="primary" :loading="addUserGroupLoading" @click="handleCreateUserGroup">
        {{ t('common.add') }}
      </n-button>
    </template>
  </n-modal>
  <user-drawer :visible="userVisible"
               :project-id="currentProjectId"
               :user-role-id="currentItem.id"
               @request-fetch-data="fetchData"
               @cancel="userVisible=false"/>
</template>

<style scoped>

</style>