<script setup lang="ts">

import {
  AuthScopeType,
  AuthTableItem,
  CurrentUserGroupItem,
  SavePermissions,
  UserGroupAuthSetting
} from "/@/models/setting/user-group.ts";
import {computed, h, inject, ref, watchEffect} from "vue";
import {AuthScopeEnum} from "/@/enums/common-enum.ts";
import type {DataTableColumns} from "naive-ui";
import {NCheckbox, NCheckboxGroup} from "naive-ui";
import {useI18n} from "vue-i18n";
import {useRequest} from "alova/client";
import {
  getGlobalUSetting,
  getOrgUSetting,
  saveGlobalUSetting,
  saveOrgUSetting
} from "/@/api/modules/setting/user-group.ts";
import {getAuthByUserGroup, saveProjectUGSetting} from "/@/api/modules/project-management/user-group.ts";

const props = withDefaults(
    defineProps<{
      current: CurrentUserGroupItem;
      savePermission?: string[];
      showBottom?: boolean;
      disabled?: boolean;
      scroll?: {
        x?: number | string;
        y?: number | string;
        minWidth?: number | string;
        maxHeight?: number | string;
      };
    }>(),
    {
      showBottom: true,
      disabled: false,
      scroll() {
        return {
          x: '800px',
          y: '100%',
        };
      },
    }
);
const systemType = inject<AuthScopeEnum>('systemType');
const {t} = useI18n();
// 是否可以保存
const canSave = ref(false);
const systemSpan = ref(1);
const projectSpan = ref(1);
const organizationSpan = ref(1);
const workstationSpan = ref(1);
const testPlanSpan = ref(1);
const bugManagementSpan = ref(1);
const caseManagementSpan = ref(1);
const uiTestSpan = ref(1);
const apiTestSpan = ref(1);
const loadTestSpan = ref(1);
const personalSpan = ref(1);
// 不可编辑的权限
const systemAdminDisabled = computed(() => {
  const adminArr = ['admin', 'org_admin', 'project_admin'];
  const {id} = props.current;
  if (adminArr.includes(id)) {
    // 系统管理员,组织管理员，项目管理员都不可编辑
    return true;
  }

  return props.disabled;
});
const columns: DataTableColumns<AuthTableItem> = [
  {
    type: 'selection'
  },
  {
    title: t('system.userGroup.function'),
    key: 'ability',
    width: 100
  },
  {
    title: t('system.userGroup.operationObject'),
    key: 'operationObject',
    width: 150
  },
  {
    title: () => h('div', {class: 'flex w-full flex-row justify-between'}, {
      default: () => [
        h('div', {}, {default: () => t('system.userGroup.auth')}),
        h(NCheckbox, {
          checked: allChecked.value,
          indeterminate: allIndeterminate.value,
          disabled: systemAdminDisabled.value || props.disabled,
          class: 'mr-[7px]',
          "onUpdate:checked": () => handleAllAuthChangeByCheckbox()
        }, {})
      ]
    }),
    key: 'phone',
    render: (row, rowIndex) => {
      return h('div', {class: 'flex flex-row items-center justify-between'}, {
        default: () => [h(NCheckboxGroup, {
          value: row.perChecked,
          "onUpdate:value": (v, e) => handleCellAuthChange(v, rowIndex, row, e)
        }, {
          default: () => {
            const result: any[] = []
            row.permissions?.forEach(permission => {
              result.push(h(NCheckbox, {
                value: permission.id,
                label: t(permission.name),
                disabled: systemAdminDisabled.value || props.disabled
              }, {}))
            })
            return result
          }
        }),
          h(NCheckbox, {
            class: 'mr-[7px]', checked: row.enable, indeterminate: row.indeterminate,
            disabled: systemAdminDisabled.value || props.disabled,
            "onUpdate:checked": (value) => handleRowAuthChange(value, rowIndex)
          }, {})
        ]
      })
    }
  },
];
const tableData = ref<AuthTableItem[]>();
const {send, loading} = useRequest((id) => {
  if (systemType === AuthScopeEnum.SYSTEM) {
    return getGlobalUSetting(id)
  } else if (systemType === AuthScopeEnum.ORGANIZATION) {
    return getOrgUSetting(id);
  } else {
    return getAuthByUserGroup(id)
  }
}, {immediate: false})
const initData = (id: string) => {
  tableData.value = []; // 重置数据，可以使表格滚动条重新计算
  send(id).then(res => {
    tableData.value = transformData(res);
    handleAllChange(true);
  })
};
const transformData = (data: UserGroupAuthSetting[]) => {
  const result: AuthTableItem[] = [];
  data.forEach((item) => {
    if (item.id === 'SYSTEM') {
      systemSpan.value = item.children?.length || 0;
    } else if (item.id === 'PROJECT') {
      projectSpan.value = item.children?.length || 0;
    } else if (item.id === 'ORGANIZATION') {
      organizationSpan.value = item.children?.length || 0;
    } else if (item.id === 'WORKSTATION') {
      workstationSpan.value = item.children?.length || 0;
    } else if (item.id === 'TEST_PLAN') {
      testPlanSpan.value = item.children?.length || 0;
    } else if (item.id === 'BUG_MANAGEMENT') {
      bugManagementSpan.value = item.children?.length || 0;
    } else if (item.id === 'CASE_MANAGEMENT') {
      caseManagementSpan.value = item.children?.length || 0;
    } else if (item.id === 'UI_TEST') {
      uiTestSpan.value = item.children?.length || 0;
    } else if (item.id === 'API_TEST') {
      apiTestSpan.value = item.children?.length || 0;
    } else if (item.id === 'LOAD_TEST') {
      loadTestSpan.value = item.children?.length || 0;
    } else if (item.id === 'PERSONAL') {
      personalSpan.value = item.children?.length || 0;
    }
    result.push(...makeData(item, item.id));
  });
  return result;
}
const makeData = (item: UserGroupAuthSetting, type: AuthScopeType) => {
  const result: AuthTableItem[] = [];
  item.children?.forEach((child, index) => {
    if (!child.license) {
      const perChecked =
          child?.permissions?.reduce((acc: string[], cur) => {
            if (cur.enable) {
              acc.push(cur.id);
            }
            return acc;
          }, []) || [];
      const perCheckedLength = perChecked.length;
      let indeterminate = false;
      if (child?.permissions) {
        indeterminate = perCheckedLength > 0 && perCheckedLength < child?.permissions?.length;
      }
      result.push({
        id: child?.id,
        license: child?.license,
        enable: child?.enable,
        permissions: child?.permissions,
        indeterminate,
        perChecked,
        ability: index === 0 ? item.name : undefined,
        operationObject: t(child.name),
        isSystem: index === 0 && type === 'SYSTEM',
        isOrganization: index === 0 && type === 'ORGANIZATION',
        isProject: index === 0 && type === 'PROJECT',
        isWorkstation: index === 0 && type === 'WORKSTATION',
        isTestPlan: index === 0 && type === 'TEST_PLAN',
        isBugManagement: index === 0 && type === 'BUG_MANAGEMENT',
        isCaseManagement: index === 0 && type === 'CASE_MANAGEMENT',
        isUiTest: index === 0 && type === 'UI_TEST',
        isLoadTest: index === 0 && type === 'LOAD_TEST',
        isApiTest: index === 0 && type === 'API_TEST',
        isPersonal: index === 0 && type === 'PERSONAL',
      });
    }
  });
  return result;
};
// 表格的总全选
const allChecked = ref(false);
const allIndeterminate = ref(false);
// 表格总全选联动触发事件
const handleAllChange = (isInit = false) => {
  if (!tableData.value) return;
  const tmpArr = tableData.value;
  const {length: allLength} = tmpArr;
  const {length} = tmpArr.filter((item) => item.enable);
  if (length === allLength) {
    allChecked.value = true;
    allIndeterminate.value = false;
  } else if (length === 0) {
    allChecked.value = false;
    allIndeterminate.value = false;
  } else {
    allChecked.value = false;
    allIndeterminate.value = true;
  }
  if (!isInit && !canSave.value) canSave.value = true;
};
// 恢复默认值
const handleReset = () => {
  if (props.current.id) {
    initData(props.current.id);
  }
};
const handleSave = async () => {
  if (!tableData.value) return;
  const permissions: SavePermissions[] = [];

  const tmpArr = tableData.value;
  tmpArr.forEach((item) => {
    item.permissions?.forEach((ele) => {
      ele.enable = item.perChecked?.includes(ele.id) || false;
      permissions.push({
        id: ele.id,
        enable: ele.enable,
      });
    });
  });
  if (systemType === AuthScopeEnum.SYSTEM) {
    await saveGlobalUSetting({
      userRoleId: props.current.id,
      permissions,
    });
  } else if (systemType === AuthScopeEnum.ORGANIZATION) {
    await saveOrgUSetting({
      userRoleId: props.current.id,
      permissions,
    });
  } else {
    await saveProjectUGSetting({
      userRoleId: props.current.id,
      permissions,
    });
  }
  canSave.value = false;
  window.$message.success(t('common.saveSuccess'));
  initData(props.current.id);
}
const handleAllAuthChangeByCheckbox = () => {
  if (!tableData.value) return;
  allChecked.value = !allChecked.value;
  allIndeterminate.value = false;
  const tmpArr = tableData.value;
  tmpArr.forEach((item) => {
    item.enable = allChecked.value;
    item.indeterminate = false;
    item.perChecked = allChecked.value ? item.permissions?.map((ele) => ele.id) : [];
  });
  if (!canSave.value) canSave.value = true;
}
const handleRowAuthChange = (value: boolean | (string | number | boolean)[], rowIndex: number) => {
  if (!tableData.value) return;
  const tmpArr = tableData.value;
  tmpArr[rowIndex].indeterminate = false;
  if (value) {
    tmpArr[rowIndex].enable = true;
    tmpArr[rowIndex].perChecked = tmpArr[rowIndex].permissions?.map((item) => item.id);
  } else {
    tmpArr[rowIndex].enable = false;
    tmpArr[rowIndex].perChecked = [];
  }
  tableData.value = [...tmpArr];
  handleAllChange();
  if (!canSave.value) canSave.value = true;
};
/**
 * 表格第三列的复选框change事件
 * @param values
 * @param rowIndex
 * @param record
 * @param e
 */
const handleCellAuthChange = (_values: (string | number | boolean)[],
                              rowIndex: number,
                              record: AuthTableItem,
                              e: Event) => {
  setAutoRead(record, (e as unknown as { actionType: string, value: string }).value);
  if (!tableData.value) return;
  const tmpArr = tableData.value;
  const length = tmpArr[rowIndex].permissions?.length || 0;
  if (record.perChecked?.length === length) {
    tmpArr[rowIndex].enable = true;
    tmpArr[rowIndex].indeterminate = false;
  } else if (record.perChecked?.length === 0) {
    tmpArr[rowIndex].enable = false;
    tmpArr[rowIndex].indeterminate = false;
  } else {
    tmpArr[rowIndex].enable = false;
    tmpArr[rowIndex].indeterminate = true;
  }
  handleAllChange();
}
/**
 * 当选中某个权限值时判断当前选中的列中有没有read权限
 * @param record
 * @param currentValue
 */
const setAutoRead = (record: AuthTableItem, currentValue: string) => {
  if (!record.perChecked?.includes(currentValue)) {
    // 如果当前没有选中则执行自动添加查询权限逻辑
    // 添加权限值
    record.perChecked?.push(currentValue);
    const preStr = currentValue.split(':')[0];
    const postStr = currentValue.split(':')[1];
    const lastEditStr = currentValue.split('+')[1]; // 编辑类权限通过+号拼接
    const existRead = record.perChecked?.some(
        (item: string) => item.split(':')[0] === preStr && item.split(':')[1] === 'READ'
    );
    const existCreate = record.perChecked?.some(
        (item: string) => item.split(':')[0] === preStr && item.split(':')[1] === 'ADD'
    );
    if (!existRead && postStr !== 'READ') {
      record.perChecked?.push(`${preStr}:READ`);
    }
    if (!existCreate && lastEditStr === 'IMPORT') {
      // 勾选导入时自动勾选新增和查询
      record.perChecked?.push(`${preStr}:ADD`);
      record.perChecked?.push(`${preStr}:READ+UPDATE`);
    }
  } else {
    // 删除权限值
    const preStr = currentValue.split(':')[0];
    const postStr = currentValue.split(':')[1];
    if (postStr === 'READ') {
      // 当前是查询 那 移除所有相关的
      record.perChecked = record.perChecked.filter((item: string) => !item.includes(preStr));
    } else {
      record.perChecked.splice(record.perChecked.indexOf(currentValue), 1);
    }
  }
}
watchEffect(() => {
  if (props.current.id) {
    initData(props.current.id);
  }
});
defineExpose({
  canSave,
  handleSave,
  handleReset,
});
</script>

<template>
  <n-spin :show="loading">
    <div class="flex h-full flex-col gap-[16px] overflow-hidden">
      <div class="group-auth-table">
        <n-data-table :bordered="false" :columns="columns" :data="tableData" :row-key="(tmp:AuthTableItem)=>tmp.id"/>

      </div>
      <div v-if="props.showBottom" v-permission="props.savePermission || []" class="footer">
        <n-button :disabled="!canSave" @click="handleReset">{{ t('system.userGroup.reset') }}</n-button>
        <n-button type="primary" v-permission="props.savePermission || []" :disabled="!canSave" @click="handleSave">
          {{ t('system.userGroup.save') }}
        </n-button>
      </div>
    </div>

  </n-spin>
</template>

<style scoped>
.footer {
  display: flex;
  justify-content: flex-end;
  padding: 24px;
  background-color: #ffffff;
  box-shadow: 0 -1px 4px rgb(2 2 2 / 10%);
  gap: 16px;
}
</style>