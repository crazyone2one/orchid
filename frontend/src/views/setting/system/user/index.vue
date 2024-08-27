<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue'
import {SimpleUserInfo, SystemRole, UpdateUserInfoParams, UserListItem} from "/@/models/setting/user.ts";
import {computed, h, onBeforeMount, Ref, ref, resolveDirective, withDirectives} from "vue";
import {BatchActionQueryParams, TableQueryParams} from "/@/models/common.ts";
import {usePagination} from "alova/client";
import {
  batchCreateUser,
  getSystemRoles,
  getUserList,
  toggleUserStatus,
  updateUserInfo
} from "/@/api/modules/setting/user.ts";
import type {DataTableColumns, DataTableRowKey, DropdownOption, FormInst, FormItemRule} from "naive-ui";
import {NFlex, NSelect, NSwitch} from "naive-ui";
import {useI18n} from "vue-i18n";
import {hasAllPermission, hasAnyPermission} from "/@/utils/permission.ts";
import TagGroup from '/@/components/o-tag-group/index.vue'
import {cloneDeep} from "lodash-es";
import BatchForm from '/@/components/o-batch-form/index.vue'
import {FormItemModel} from "/@/components/o-batch-form/types.ts";
import {validateEmail, validatePhone} from "/@/utils/validate.ts";
import OButton from "/@/components/o-button/index.vue";
import TableMoreAction from '/@/components/o-table-more-action/index.vue'
import {characterLimit} from "/@/utils";

type UserModalMode = 'create' | 'edit';

interface UserForm {
  list: SimpleUserInfo[];
  userGroup: string[];
}

const {t} = useI18n()
const reqParam = ref<TableQueryParams>({
  current: 1,
  pageSize: 6,
  keyword: ''
})
const defaultUserForm = {
  list: [
    {
      name: '',
      email: '',
      phone: '',
    },
  ],
  userGroup: [],
};
const permission = resolveDirective('permission')
const showModal = ref(false);
const keyword = ref('');
const userFormRef = ref<FormInst | null>(null)
const userFormMode = ref<UserModalMode>('create');
const userForm = ref<UserForm>(cloneDeep(defaultUserForm));
const userGroupOptions = ref<SystemRole[]>([]);
const batchFormRef = ref<InstanceType<typeof BatchForm>>();
const checkedRowKeys = ref<DataTableRowKey[]>([])
const hasOperationSysUserPermission = computed(() =>
    hasAnyPermission(['SYSTEM_USER:READ+UPDATE', 'SYSTEM_USER:READ+DELETE'])
);
const handleCheck = (rowKeys: DataTableRowKey[]) => {
  checkedRowKeys.value = rowKeys;
}
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
    width: hasOperationSysUserPermission.value ? 110 : 50,
    render(row) {
      if (!row.enable) {
        return withDirectives(h(OButton, {
          text: true,
          type: 'error',
          content: t('system.user.delete')
        }, {}), [[permission, ['SYSTEM_USER:READ+DELETE']]]);
      } else {
        return h(NFlex, {}, {
          default: () =>
              [
                withDirectives(h(OButton, {
                  text: true,
                  content: t('system.user.editUser')
                }, {}), [[permission, ['SYSTEM_USER:READ+UPDATE']]]),
                withDirectives(h(TableMoreAction, {
                  list: tableActions,
                  onSelect: ($event) => handleSelect($event, row)
                }, {}), [[permission, ['SYSTEM_USER:READ+UPDATE', 'SYSTEM_USER:READ+DELETE']]])
              ]
        })
      }
    }
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
  if (newValue) {
    enableUser(record);
  } else {
    disabledUser(record);
  }
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
const showUserModal = (mode: UserModalMode, record?: UserListItem) => {
  showModal.value = true;
  userFormMode.value = mode
}
const tableActions: DropdownOption[] = [
  {
    label: t('system.user.resetPassword'),
    key: 'resetPassword',
    permission: ['SYSTEM_USER:READ+UPDATE'],
  },
  {
    type: 'divider',
    key: 'd1'
  },
  {
    label: t('system.user.delete'),
    key: 'delete',
    permission: ['SYSTEM_USER:READ+DELETE'],
  },
];
const handleSelect = (item: DropdownOption, record: UserListItem) => {
  switch (item.key) {
    case 'resetPassword':
      resetPassword(record);
      break;
    case 'delete':
      deleteUser(record);
      break;
    default:
      break;
  }
};
const resetPassword = (record?: UserListItem, isBatch?: boolean, params?: BatchActionQueryParams) => {
  let title = t('system.user.resetPswTip', {name: characterLimit(record?.name)});
  let selectIds = [record?.id || ''];
  if (isBatch) {
    title = t('system.user.batchResetPswTip', {count: params?.currentSelectCount || checkedRowKeys.value.length});
    selectIds = checkedRowKeys.value as string[];
  }
  let content = t('system.user.resetPswContent');
  if (record && record.id === 'admin') {
    content = t('system.user.resetAdminPswContent');
  }
  window.$dialog.warning({
    title,
    content,
    positiveText: t('system.user.resetPswConfirm'),
    negativeText: t('system.user.resetPswCancel'),
    onPositiveClick: () => {
      // todo 重置密码
      window.$message.info('重置密码...')
    }
  })
};
const deleteUser = (record?: UserListItem, isBatch?: boolean, params?: BatchActionQueryParams) => {
  let title = t('system.user.deleteUserTip', {name: characterLimit(record?.name)});
  let selectIds = [record?.id || ''];
  if (isBatch) {
    title = t('system.user.batchDeleteUserTip', {count: params?.currentSelectCount || checkedRowKeys.value.length});
    selectIds = checkedRowKeys.value as string[];
  }
  let content = t('system.user.deleteUserContent');
  window.$dialog.error({
    title,
    content,
    positiveText: t('system.user.deleteUserConfirm'),
    negativeText: t('system.user.deleteUserCancel'),
    onPositiveClick: () => {
      // todo 删除用户
      window.$message.info('删除用户...')
    }
  })
};
const batchFormModels: Ref<FormItemModel[]> = ref([
  {
    field: 'name',
    type: 'input',
    label: 'system.user.createUserName',
    rules: [{
      trigger: ['input', 'blur'],
      required: true, message: t('system.user.createUserNameNotNull'), validator(_rule: FormItemRule, value: string) {
        if (value === '') {
          return new Error(t('system.user.createUserNameNotNull'))
        } else if (value.length > 255) {
          return new Error(t('system.user.createUserNameOverLength'))
        }
      }
    }],
    placeholder: 'system.user.createUserNamePlaceholder',
  },
  {
    field: 'email',
    type: 'input',
    label: 'system.user.createUserEmail',
    rules: [
      {required: true, message: t('system.user.createUserEmailNotNull')},
      {
        validator(_rule: FormItemRule, value: string) {
          if (value === '') {
            return new Error(t('system.user.createUserEmailNotNull'))
          } else if (!validateEmail(value)) {
            return new Error(t('system.user.createUserEmailErr'))
          }
        }
      },
      {notRepeat: true, message: 'system.user.createUserEmailNoRepeat'},
    ],
    placeholder: 'system.user.createUserEmailPlaceholder',
  },
  {
    field: 'phone',
    type: 'input',
    label: 'system.user.createUserPhone',
    rules: [{
      validator(_rule: FormItemRule, value: string) {
        if (value !== null && value !== '' && value !== undefined && !validatePhone(value)) {
          return new Error(t('system.user.createUserPhoneErr'))
        }
      }
    }],
    placeholder: 'system.user.createUserPhonePlaceholder',
  },
])
const isBatchFormChange = ref(false);
const handleBatchFormChange = () => {
  isBatchFormChange.value = true
}
const handleBeforeClose = () => {
  if (isBatchFormChange.value) {
    window.$dialog.warning({
      title: t('common.tip'),
      content: t('system.user.closeTip'),
      positiveText: t('common.close'),
      onPositiveClick: () => cancelCreate()
    });
  } else {
    cancelCreate();
  }
}
const cancelCreate = () => {
  showModal.value = false;
  resetUserForm();
}
const resetUserForm = () => {
  userForm.value.list = [];
  userFormRef.value?.restoreValidation();
  userForm.value.userGroup = userGroupOptions.value.filter(e => e.selected === true).map((e: SystemRole) => e.id)
}
const beforeCreateUser = () => {
  if (userFormMode.value === 'create') {
    userFormValidate(createUser);
  } else {
    userFormValidate(updateUser);
  }
}
const userFormValidate = (cb: () => Promise<any>) => {
  userFormRef.value?.validate(errors => {
    if (errors) {
      return
    }
    batchFormRef.value?.formValidate(async (list: any) => {
      try {
        userForm.value.list = list;
        await cb();
      } catch (e) {
        console.log(e)
      }
    })
  })
}
const updateUser = async () => {
  const activeUser = userForm.value.list[0];
  const params = {
    id: activeUser.id as string,
    name: activeUser.name,
    email: activeUser.email,
    phone: activeUser.phone,
    userRoleIdList: userForm.value.userGroup,
  };
  await updateUserInfo(params);
  window.$message.success(t('system.user.updateUserSuccess'));
  fetchData()
}
const createUser = async (isContinue?: boolean) => {
  const params = {
    userInfoList: userForm.value.list,
    userRoleIdList: userForm.value.userGroup,
  };
  const res = await batchCreateUser(params);
  if (res.errorEmails !== null) {

  } else {
    window.$message.success(t('system.user.addUserSuccess'))
    if (!isContinue) {
      cancelCreate();
    }
    fetchData();
  }
}
const enableUser = (record?: UserListItem, isBatch?: boolean, params?: BatchActionQueryParams) => {
  let title = t('system.user.enableUserTip', {name: characterLimit(record?.name)});
  let selectIds = [record?.id || ''];
  if (isBatch) {
    title = t('system.user.batchEnableUserTip', {count: params?.currentSelectCount || checkedRowKeys.value.length});
    selectIds = checkedRowKeys.value as string[];
  }
  window.$dialog.info({
    title,
    content: t('system.user.enableUserContent'),
    positiveText: t('system.user.enableUserConfirm'),
    negativeText: t('system.user.enableUserCancel'),
    maskClosable: false,
    onPositiveClick: async () => {
      try {
        await toggleUserStatus({
          selectIds,
          selectAll: !!params?.selectAll,
          excludeIds: params?.excludeIds || [],
          condition: {keyword: keyword.value},
          enable: false,
        });
        window.$message.success(t('system.user.enableUserSuccess'));
        checkedRowKeys.value = []
        fetchData();
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    }
  })
}
const disabledUser = (record?: UserListItem, isBatch?: boolean, params?: BatchActionQueryParams) => {
  let title = t('system.user.disableUserTip', {name: characterLimit(record?.name)});
  let selectIds = [record?.id || ''];
  if (isBatch) {
    title = t('system.user.batchDisableUserTip', {count: params?.currentSelectCount || checkedRowKeys.value.length});
    selectIds = checkedRowKeys.value as string[];
  }
  window.$dialog.warning({
    title,
    content: t('system.user.disableUserContent'),
    positiveText: t('system.user.disableUserConfirm'),
    negativeText: t('system.user.disableUserCancel'),
    maskClosable: false,
    onPositiveClick: async () => {
      try {
        await toggleUserStatus({
          selectIds,
          selectAll: !!params?.selectAll,
          excludeIds: params?.excludeIds || [],
          condition: {keyword: keyword.value},
          enable: false,
        });
        window.$message.success(t('system.user.disableUserSuccess'));
        checkedRowKeys.value = []
        fetchData();
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    }
  })
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
                  class="mr-3" type="primary" @click="showUserModal('create')">
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
    <n-data-table :bordered="false" :columns="columns" :data="data" :row-key="(tmp:UserListItem)=>tmp.id"
                  @update:checked-row-keys="handleCheck"/>
  </o-card>
  <n-modal v-model:show="showModal" preset="dialog" :mask-closable="false" :closable="false" style="width: 40%">
    <template #header>
      <div>
        {{ userFormMode === 'create' ? t('system.user.createUserModalTitle') : t('system.user.editUserModalTitle') }}
      </div>
    </template>
    <div>
      <n-form ref="userFormRef" :model="userForm" label-placement="left">
        <batch-form ref="batchFormRef" :models="batchFormModels" add-text="system.user.addUser"
                    :form-mode="userFormMode"
                    :default-vals="userForm.list"
                    max-height="250px" @change="handleBatchFormChange"/>
        <n-form-item :label="$t('system.user.createUserUserGroup')">
          <n-select v-model:value="userForm.userGroup" multiple :options="userGroupOptions" label-field="name"
                    value-field="id" clearable
                    :placeholder="t('system.user.createUserUserGroupPlaceholder')"/>
        </n-form-item>
      </n-form>
    </div>
    <template #action>
      <n-button secondary @click="handleBeforeClose">{{ t('system.user.editUserModalCancelCreate') }}</n-button>
      <n-button v-if="userFormMode === 'create'" secondary>{{
          t('system.user.editUserModalSaveAndContinue')
        }}
      </n-button>
      <n-button type="primary" @click="beforeCreateUser">
        {{ t(userFormMode === 'create' ? 'system.user.editUserModalCreateUser' : 'system.user.editUserModalEditUser') }}
      </n-button>
    </template>
  </n-modal>
</template>

<style scoped>

</style>