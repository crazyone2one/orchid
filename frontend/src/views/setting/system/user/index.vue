<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue'
import {SystemRole, UpdateUserInfoParams, UserListItem} from "/@/models/setting/user.ts";
import {computed, h, onBeforeMount, ref} from "vue";
import {TableQueryParams} from "/@/models/common.ts";
import {usePagination} from "alova/client";
import {getSystemRoles, getUserList, updateUserInfo} from "/@/api/modules/setting/user.ts";
import {DataTableColumns, NSelect, NSwitch} from "naive-ui";
import {useI18n} from "vue-i18n";
import {hasAllPermission, hasAnyPermission} from "/@/utils/permission.ts";
import TagGroup from '/@/components/o-tag-group/index.vue'

const {t} = useI18n()
const reqParam = ref<TableQueryParams>({
  current: 1,
  pageSize: 6,
  keyword: ''
})
const userGroupOptions = ref<SystemRole[]>([]);
const hasOperationSysUserPermission = computed(() =>
    hasAnyPermission(['SYSTEM_USER:READ+UPDATE', 'SYSTEM_USER:READ+DELETE'])
);
const columns: DataTableColumns<UserListItem> = [
  {
    type: 'selection'
  },
  {
    title: t('system.user.userName'),
    key: 'email',
    ellipsis: {tooltip: true}
  },
  {
    title: t('system.user.tableColumnName'),
    key: 'name',
    ellipsis: {tooltip: true}
  },
  {
    title: t('system.user.tableColumnEmail'),
    key: 'email',
    ellipsis: {tooltip: true}
  },
  {
    title: t('system.user.tableColumnPhone'),
    key: 'phone',
    ellipsis: {tooltip: true},
    width: 140
  },
  {
    title: t('system.user.tableColumnOrg'),
    key: 'organizationList',
    width: 300,
    render(row) {
      return h(TagGroup, {tagList: row.organizationList, type: 'primary'});
    }
  },
  {
    title: t('system.user.tableColumnUserGroup'),
    key: 'userRoleList',
    width: 300,
    render(row) {
      if (!row.selectUserGroupVisible) {
        return h(TagGroup, {tagList: row.userRoleList, type: 'primary', onClick: () => handleTagClick(row)});
      }
      return h(NSelect, {
        class: 'w-full max-w-[300px]',
        multiple: true,
        clearable: true,
        placeholder: t('system.user.createUserUserGroupPlaceholder'),
        size: 'small',
        value: row.userGroupIds,
        options: userGroupOptions.value,
        valueField: "id",
        labelField: 'name',
        disabled: row.selectUserGroupLoading,
        loading: row.selectUserGroupLoading,
        "onUpdate:show": (value) => handleUserGroupChange(value, row),
        "onUpdate:value": (value) => row.userGroupIds = value
      }, {})
    }
  },
  {
    title: t('system.user.tableColumnStatus'),
    key: 'enable',
    render(row) {
      return h(NSwitch, {
        value: row.enable,
        disabled: !hasAnyPermission(['SYSTEM_USER:READ+UPDATE']),
        size: 'small',
        "onUpdate:value": (value) => handleEnableOrDisableUser(row, value)
      }, {})
    }
  },
  {
    title: hasOperationSysUserPermission.value ? t('system.organization.operation') : '',
    key: 'operation',
    fixed: 'right',
    width: hasOperationSysUserPermission.value ? 110 : 50
  }
]
const {send: fetchData, data} = usePagination(() => getUserList(reqParam.value), {
  immediate: false,
  initialData: {
    total: 0,
    data: []
  },
  data: response => response.records,
  total: response => response.total
})
const handleEnableOrDisableUser = (record: UserListItem, newValue: string | number | boolean) => {
  console.log(record);
  console.log(newValue);
  // todo 禁用用户
}
const handleTagClick = (record: UserListItem) => {
  if (hasAllPermission(['SYSTEM_USER:READ+UPDATE', 'SYSTEM_USER_ROLE:READ'])) {
    record.selectUserGroupVisible = true;
  }
};
const handleUserGroupChange = async (val: boolean, record: UserListItem & Record<string, any>) => {
  try {
    if (!val) {
      record.selectUserGroupLoading = true;
      const params: UpdateUserInfoParams = {
        id: record.id,
        name: record.name,
        email: record.email,
        phone: record.phone,
        userRoleIdList: record.userGroupIds as string[],
      };
      await updateUserInfo(params)
      window.$message.success(t('system.user.updateUserSuccess'))
      record.selectUserGroupVisible = false;
    }
  } catch (error) {
    console.log(error);
  } finally {
    record.selectUserGroupLoading = false;
  }
}
const init = async () => {
  try {
    userGroupOptions.value = await getSystemRoles();
    if (userGroupOptions.value.length) {
      // userForm.value.userGroup = userGroupOptions.value.filter((e: SystemRole) => e.selected === true);
    }
  } catch (e) {
    console.log(e);
  }
}
onBeforeMount(() => {
  init();
  fetchData()
});
</script>

<template>
  <o-card simple>
    <template #headerLeft>
      <div>
        <n-button v-permission.all="['SYSTEM_USER:READ+ADD']"
                  class="mr-3" type="primary">
          {{ $t('system.user.createUser') }}
        </n-button>
        <n-button v-permission.all="['SYSTEM_USER:READ+IMPORT']"
                  class="mr-3" type="primary">
          {{ $t('system.user.importUser') }}
        </n-button>
      </div>
    </template>
    <template #headerRight>
      <n-input :placeholder="$t('system.user.searchUser')" clearable/>
    </template>
    <n-data-table :bordered="false" :columns="columns" :data="data" :row-key="(tmp:UserListItem)=>tmp.id"/>
  </o-card>
</template>

<style scoped>

</style>