<script setup lang="ts">
import ODrawer from '/@/components/o-drawer/index.vue';
import {useI18n} from "vue-i18n";
import {ref, watch} from "vue";
import type {DataTableColumns} from "naive-ui";
import {UserTableItem} from "/@/models/setting/user-group.ts";
import Pagination from "/@/components/o-pagination/index.vue";
import {TableQueryParams} from "/@/models/common.ts";
import {usePagination} from "alova/client";
import {postUserByUserGroup} from "/@/api/modules/project-management/user-group.ts";
import AddUserModal from "/@/views/project-management/project-permission/user-group/AddUserModal.vue";

export interface projectDrawerProps {
  visible: boolean;
  userRoleId: string;
  projectId: string;
}

const {t} = useI18n();
const props = defineProps<projectDrawerProps>();
const emit = defineEmits<{
  (e: 'cancel'): void;
  (e: 'requestFetchData'): void;
}>();
const currentVisible = ref(props.visible);
const userVisible = ref(false);
const columns: DataTableColumns<UserTableItem> = [
  {
    type: 'selection'
  },
  {
    title: t('system.organization.userName'),
    key: 'name',
    width: 200
  },
  {
    title: t('system.organization.email'),
    key: 'email',
    width: 200
  },
  {
    title: t('system.organization.phone'),
    key: 'phone',
  },
  {
    title: t('common.operation'),
    key: 'operation',
    fixed: 'right',
    width: 60,
  }
]
const reqParam = ref<TableQueryParams>({
  current: 1,
  pageSize: 10,
  keyword: ''
});
const {send: fetchData, data, page, pageSize, total} = usePagination((page, pageSize) => {
  reqParam.value.current = page
  reqParam.value.pageSize = pageSize
  return postUserByUserGroup(reqParam.value)
}, {
  immediate: false,
  initialData: {
    total: 0,
    data: []
  },
  data: response => response.records,
  total: response => response.total,
  // watchingStates: [reqParam]
});
const handleSetPage = (param: number) => page.value = param
const handleSetPageSize = (param: number) => pageSize.value = param;

const handleCancel = () => {
  reqParam.value.keyword = '';
  emit('cancel');
};
const handleAddMember = () => {
  userVisible.value = true;
}
const handleHideUserModal = (shouldSearch: boolean) => {
  userVisible.value = false;
  if (shouldSearch) {
    fetchData();
    emit('requestFetchData');
  }
}
watch(
    () => props.visible,
    (visible) => {
      currentVisible.value = visible;
      if (visible) {
        reqParam.value.projectId = props.projectId
        reqParam.value.userRoleId = props.userRoleId
        fetchData();
      }
    }
);
</script>

<template>
  <o-drawer :visible="currentVisible" width="680" :footer="false" :title="t('system.organization.addMember')"
            :mask-closable="false" @close="handleCancel">
    <div class="flex flex-row justify-between">
      <n-button type="primary" @click="handleAddMember">
        {{ t('system.organization.addMember') }}
      </n-button>
      <div>
        <n-input
            v-model:model-value="reqParam.keyword"
            clearable
            :placeholder="t('system.user.searchUser')"
            class="w-[230px]"
            @clear="fetchData"
        />
      </div>
    </div>
    <n-data-table :bordered="false" :columns="columns" :data="data" :row-key="(tmp:UserTableItem)=>tmp.id"/>
    <div class="mt-8">
      <pagination :page-size="pageSize" :page="page" :count="total"
                  @update-page="handleSetPage"
                  @update-page-size="handleSetPageSize"/>
    </div>
  </o-drawer>
  <add-user-modal :project-id="props.projectId"
                  :user-role-id="props.userRoleId"
                  :visible="userVisible"
                  @cancel="handleHideUserModal"/>
</template>

<style scoped>

</style>