<script setup lang="ts">
import {computed, inject, ref, watch, watchEffect} from "vue";
import {AuthScopeEnum} from "/@/enums/common-enum.ts";
import {useI18n} from "vue-i18n";
import {useAppStore} from "/@/store";
import type {DataTableColumns} from "naive-ui";
import {CurrentUserGroupItem, UserTableItem} from "/@/models/setting/user-group.ts";
import {usePagination} from "alova/client";
import {TableQueryParams} from "/@/models/common.ts";
import {postOrgUserByUserGroup, postUserByUserGroup} from "/@/api/modules/setting/user-group.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";
import Pagination from "/@/components/o-pagination/index.vue";

const systemType = inject<AuthScopeEnum>('systemType');
const {t} = useI18n();
const appStore = useAppStore();
const currentOrgId = computed(() => appStore.currentOrgId);
const props = defineProps<{
  keyword: string;
  current: CurrentUserGroupItem;
  deletePermission?: string[];
  readPermission?: string[];
  updatePermission?: string[];
}>();
const current = ref<CurrentUserGroupItem>({})
const columns: DataTableColumns<UserTableItem> = [
  {
    type: 'selection'
  },
  {
    title: t('system.userGroup.name'),
    key: 'name',
    ellipsis: {tooltip: true}
  },
  {
    title: t('system.userGroup.email'),
    key: 'email',
    ellipsis: {tooltip: true}
  },
  {
    title: t('system.userGroup.phone'),
    key: 'phone',
    ellipsis: {tooltip: true},
  },
  {
    title: t('system.userGroup.operation'),
    key: 'operation',
    fixed: 'right', width: 100
  }];
const reqParam = ref<TableQueryParams>({
  current: 1,
  pageSize: 6,
  keyword: ''
})
const {
  loading,
  data,
  page,
  pageSize,
  total, send
} = usePagination(
    // Method实例获取函数，它将接收page和pageSize，并返回一个Method实例
    (page, pageSize) => {
      reqParam.value.current = page;
      reqParam.value.pageSize = pageSize;
      return systemType === AuthScopeEnum.SYSTEM ? postUserByUserGroup(reqParam.value) : postOrgUserByUserGroup(reqParam.value)
    },
    {
      immediate: false,
      // 请求前的初始数据（接口返回的数据格式）
      initialData: {
        total: 0,
        data: []
      },
      data: response => response.records,
      total: response => response.total,
      initialPage: 1, // 初始页码，默认为1
      initialPageSize: 10 // 初始每页数据条数，默认为10
    }
);
const handlePermission = (permission: string[], cb: () => void) => {
  if (!hasAnyPermission(permission)) {
    return false;
  }
  cb();
};
const fetchData = async () => {
  handlePermission(props.readPermission || [], async () => {
    reqParam.value.keyword = props.keyword
    await send();
  });
};
const handleSetPage = (param: number) => page.value = param
const handleSetPageSize = (param: number) => pageSize.value = param
watch([current, currentOrgId], () => {
  if (systemType === AuthScopeEnum.SYSTEM) {
    reqParam.value.roleId = props.current.id
  } else if (systemType === AuthScopeEnum.ORGANIZATION) {
    reqParam.value.userRoleId = props.current.id
    reqParam.value.organizationId = currentOrgId.value
  }
  fetchData();
}, {immediate: true})
watchEffect(() => {
  current.value = props.current
})
defineExpose({
  fetchData,
});
</script>

<template>
  <n-spin :show="loading">
    <div class="px-[16px]">
      <n-data-table :bordered="false" :columns="columns" :data="data" :row-key="(tmp:UserTableItem)=>tmp.id"/>
      <div class="mt-8">
        <pagination :page-size="pageSize" :page="page" :count="total"
                    @update-page="handleSetPage"
                    @update-page-size="handleSetPageSize"/>
      </div>
    </div>
  </n-spin>
</template>

<style scoped>

</style>