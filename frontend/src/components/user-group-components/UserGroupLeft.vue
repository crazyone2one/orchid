<script setup lang="ts">
import {computed, inject, ref} from "vue";
import {AuthScopeEnum} from "/@/enums/common-enum.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";
import {getOrgUserGroupList, getProjectUserGroupList, getUserGroupList} from "/@/api/modules/setting/user-group.ts";
import {useAppStore} from "/@/store";
import {CurrentUserGroupItem, PopVisible, UserGroupItem} from "/@/models/setting/user-group.ts";
import {useI18n} from "vue-i18n";
import CreateOrUpdateUserGroup from "/@/components/user-group-components/CreateOrUpdateUserGroup.vue";
import {useRequest} from "alova/client";
import type {DropdownOption} from "naive-ui";
import MoreAction from '/@/components/o-table-more-action/index.vue'
import {characterLimit} from "/@/utils";
import AddUserModal from "/@/components/user-group-components/AddUserModal.vue";

const systemType = inject<AuthScopeEnum>('systemType');
const appStore = useAppStore()
const {t} = useI18n()
const pattern = ref('')
const emit = defineEmits<{
  (e: 'handleSelect', element: UserGroupItem): void;
  (e: 'addUserSuccess', id: string): void;
}>();
const props = defineProps<{
  addPermission: string[];
  updatePermission: string[];
  isGlobalDisable: boolean;
}>();
const addUserModalRef = ref<InstanceType<typeof AddUserModal> | null>(null);
const userGroupList = ref<UserGroupItem[]>([]);
const currentItem = ref<CurrentUserGroupItem>({id: '', name: '', internal: false, type: AuthScopeEnum.SYSTEM});
const currentId = ref('');
const popVisible = ref<PopVisible>({});
// 系统用户组列表
const systemUserGroupList = computed(() => {
  return userGroupList.value.filter((ele) => ele.type === AuthScopeEnum.SYSTEM);
});

// 组织用户组列表
const orgUserGroupList = computed(() => {
  return userGroupList.value.filter((ele) => ele.type === AuthScopeEnum.ORGANIZATION);
});
// 项目用户组列表
const projectUserGroupList = computed(() => {
  return userGroupList.value.filter((ele) => ele.type === AuthScopeEnum.PROJECT);
});

const showSystem = computed(() => systemType === AuthScopeEnum.SYSTEM);
const showOrg = computed(() => systemType === AuthScopeEnum.SYSTEM || systemType === AuthScopeEnum.ORGANIZATION);
const showProject = computed(() => systemType === AuthScopeEnum.SYSTEM || systemType === AuthScopeEnum.PROJECT);
// 系统用户组Toggle
const systemToggle = ref(true);
// 组织用户组Toggle
const orgToggle = ref(true);
// 项目用户组Toggle
const projectToggle = ref(true);
// 系统用户创建用户组visible
const systemUserGroupVisible = ref(false);
// 组织用户创建用户组visible
const orgUserGroupVisible = ref(false);
// 项目用户创建用户组visible
const projectUserGroupVisible = ref(false);
const {send: fetchUserGroupList} = useRequest(() => getUserGroupList(), {immediate: false, force: true})
const {send: fetchOrgUserGroupList} = useRequest((orgId) => getOrgUserGroupList(orgId), {immediate: false, force: true})
const {send: fetchProjectUserGroupList} = useRequest((orgId) => getProjectUserGroupList(orgId), {
  immediate: false,
  force: true
})
const initData = async (id?: string, isSelect = true) => {
  try {
    let res: UserGroupItem[] = [];
    if (systemType === AuthScopeEnum.SYSTEM && hasAnyPermission(['SYSTEM_USER_ROLE:READ'])) {
      res = await fetchUserGroupList();
    } else if (systemType === AuthScopeEnum.ORGANIZATION && hasAnyPermission(['ORGANIZATION_USER_ROLE:READ'])) {
      res = await fetchOrgUserGroupList(appStore.currentOrgId);
    } else if (systemType === AuthScopeEnum.PROJECT && hasAnyPermission(['PROJECT_GROUP:READ'])) {
      res = await fetchProjectUserGroupList(appStore.currentProjectId);
    }
    if (res.length > 0) {
      userGroupList.value = res;
      if (isSelect) {
        if (id) {
          const item = res.find((i) => i.id === id);
          if (item) {
            handleListItemClick(item);
          } else {
            window.$message.warning(t('common.resourceDeleted'));
            handleListItemClick(res[0])
          }
        } else {
          handleListItemClick(res[0]);
        }
      }
      const tmpObj: PopVisible = {};
      res.forEach((element) => {
        tmpObj[element.id] = {visible: false, authScope: element.type, defaultName: '', id: element.id};
      });
      popVisible.value = tmpObj;
    }
  } catch (error) {
    console.error(error);
  }
}
const handleListItemClick = (element: UserGroupItem) => {
  const {id, name, type, internal} = element;
  currentItem.value = {id, name, type, internal};
  currentId.value = id;
  emit('handleSelect', element);
}
const handleCreateUserGroup = (id: string) => {
  initData(id);
};
const handleCreateUG = (scoped: AuthScopeEnum) => {
  if (scoped === AuthScopeEnum.SYSTEM) {
    systemUserGroupVisible.value = true;
  } else if (scoped === AuthScopeEnum.ORGANIZATION) {
    orgUserGroupVisible.value = true;
  } else if (scoped === AuthScopeEnum.PROJECT) {
    projectUserGroupVisible.value = true;
  }
}
const isSystemShowAll = computed(() => {
  return hasAnyPermission([...props.updatePermission, 'SYSTEM_USER_ROLE:READ+DELETE']);
});
const isOrdShowAll = computed(() => {
  return hasAnyPermission([...props.updatePermission, 'ORGANIZATION_USER_ROLE:READ+DELETE']);
});
const isProjectShowAll = computed(() => {
  return hasAnyPermission([...props.updatePermission, 'PROJECT_GROUP:READ+DELETE']);
});
const systemMoreAction: DropdownOption[] = [
  {
    label: t('system.userGroup.rename'),
    danger: false,
    key: 'rename',
    permission: props.updatePermission,
  },
  {
    type: 'divider',
    key: 'd1'
  },
  {
    label: t('system.userGroup.delete'),
    danger: true,
    key: 'delete',
    permission: ['SYSTEM_USER_ROLE:READ+DELETE'],
  },
];
const orgMoreAction: DropdownOption[] = [
  {
    label: t('system.userGroup.rename'),
    danger: false,
    key: 'rename',
    permission: props.updatePermission,
  },
  {
    type: 'divider',
    key: 'd1'
  },
  {
    label: t('system.userGroup.delete'),
    danger: true,
    key: 'delete',
    permission: ['ORGANIZATION_USER_ROLE:READ+DELETE'],
  },
];
const projectMoreAction: DropdownOption[] = [
  {
    label: t('system.userGroup.rename'),
    danger: false,
    key: 'rename',
    permission: props.updatePermission,
  },
  {
    type: 'divider',
    key: 'd1'
  },
  {
    label: t('system.userGroup.delete'),
    danger: true,
    key: 'delete',
    permission: ['PROJECT_GROUP:READ+DELETE'],
  },
];
const userModalVisible = ref(false);
// 点击添加成员
const handleAddMember = () => {
  userModalVisible.value = true;
};
const handleRenameCancel = (element: UserGroupItem, id?: string) => {
  if (id) {
    initData(id, true);
  }
  popVisible.value[element.id].visible = false;
};

const handleSelect = (item: DropdownOption, id: string, authScope: AuthScopeEnum) => {
  const tmpObj = userGroupList.value.filter((ele) => ele.id === id)[0];
  if (item.key === 'rename') {
    popVisible.value[id] = {visible: true, authScope, defaultName: tmpObj.name, id};
  }
  if (item.key === 'delete') {
    let content = '';
    switch (authScope) {
      case AuthScopeEnum.SYSTEM:
        content = t('system.userGroup.beforeDeleteUserGroup');
        break;
      case AuthScopeEnum.ORGANIZATION:
        content = t('org.userGroup.beforeDeleteUserGroup');
        break;
      default:
        content = t('project.userGroup.beforeDeleteUserGroup');
        break;
    }
    window.$dialog.error({
      title: t('system.userGroup.isDeleteUserGroup', {name: characterLimit(tmpObj.name)}),
      content,
      positiveText: t('system.userGroup.confirmDelete'),
      negativeText: t('system.userGroup.cancel')
    });
  }
};
const handleAddUserCancel = (shouldSearch: boolean) => {
  userModalVisible.value = false;
  if (shouldSearch) {
    emit('addUserSuccess', currentId.value);
  }
}
defineExpose({
  initData,
});
</script>

<template>
  <div class="flex flex-col px-[16px] pb-[16px]">
    <div class="sticky top-0 z-[999] bg-white pb-[8px] pt-[16px]">
      <n-input v-model:value="pattern" size="small" placeholder="搜索"/>
    </div>
    <div v-if="showSystem" v-permission="['SYSTEM_USER_ROLE:READ']" class="mt-2">
      <div class="flex items-center justify-between px-[4px] py-[7px]">
        <div class="flex flex-row items-center gap-1">
          <n-icon v-if="systemToggle" size="16" color="#5A5A5A" @click="systemToggle = false">
            <div class="i-ic-outline-expand-more"/>
          </n-icon>
          <n-icon v-else size="16" color="#5A5A5A" @click="systemToggle = true">
            <div class="i-ic-outline-expand-less"/>
          </n-icon>
          <div class="text-[14px]">
            {{ t('system.userGroup.systemUserGroup') }}
          </div>
        </div>
        <create-or-update-user-group
            :list="systemUserGroupList"
            :visible="systemUserGroupVisible"
            :auth-scope="AuthScopeEnum.SYSTEM"
            @cancel="systemUserGroupVisible = false"
            @submit="handleCreateUserGroup"
        >
          <n-tooltip>
            <template #trigger>
              <n-icon v-permission="props.addPermission" size="20" class="text-indigo-500 hover:text-indigo-400"
                      @click="handleCreateUG(AuthScopeEnum.SYSTEM)">
                <div class="i-carbon-add-alt"/>
              </n-icon>
            </template>
            {{ `创建${$t('system.userGroup.systemUserGroup')}` }}
          </n-tooltip>
        </create-or-update-user-group>
      </div>
      <Transition>
        <div v-if="systemToggle">
          <div v-for="item in systemUserGroupList" :key="item.id"
               :class="{ '!bg-green-200': item.id === currentId }"
               @click="handleListItemClick(item)">
            <create-or-update-user-group :list="systemUserGroupList" v-bind="popVisible[item.id]"
                                         @cancel="handleRenameCancel(item)"
                                         @submit="handleRenameCancel(item, item.id)">
              <div class="flex max-w-[100%] grow flex-row items-center justify-between">
                <div class="list-item-name one-line-text">{{ item.name }}</div>
                <div v-if="item.type===systemType || (isSystemShowAll &&
                      !item.internal &&
                      (item.scopeId !== 'global' || !isGlobalDisable) &&
                      systemMoreAction.length > 0)"
                     class="list-item-action flex flex-row items-center gap-[8px] opacity-0"
                     :class="{ '!opacity-100': item.id === currentId }">
                  <div v-if="item.type === systemType" class="icon-button">
                    <n-icon v-permission="props.updatePermission" size="16" @click="handleAddMember">
                      <div class="i-carbon-add-alt"/>
                    </n-icon>
                  </div>
                  <more-action
                      v-if="isSystemShowAll && !item.internal && (item.scopeId !== 'global' || !isGlobalDisable) && systemMoreAction.length > 0"
                      :list="systemMoreAction"
                      @select="($event) => handleSelect($event, item.id,AuthScopeEnum.SYSTEM)"/>
                </div>
              </div>
            </create-or-update-user-group>
          </div>
          <n-divider class="my-[0px] mt-[6px]"/>
        </div>
      </Transition>
    </div>

    <div v-if="showOrg" v-permission="['ORGANIZATION_USER_ROLE:READ']" class="mt-2">
      <div class="flex items-center justify-between px-[4px] py-[7px]">
        <div class="flex flex-row items-center gap-1">
          <n-icon v-if="orgToggle" size="16" color="#5A5A5A" @click="orgToggle = false">
            <div class="i-ic-outline-expand-more"/>
          </n-icon>
          <n-icon v-else size="16" color="#5A5A5A" @click="orgToggle = true">
            <div class="i-ic-outline-expand-less"/>
          </n-icon>
          <div class="text-[14px]">
            {{ t('system.userGroup.orgUserGroup') }}
          </div>
        </div>
        <create-or-update-user-group :list="orgUserGroupList"
                                     :visible="orgUserGroupVisible"
                                     :auth-scope="AuthScopeEnum.ORGANIZATION"
                                     @cancel="orgUserGroupVisible = false"
                                     @submit="handleCreateUserGroup">
          <n-tooltip>
            <template #trigger>
              <n-icon v-permission="props.addPermission" size="20" class="text-indigo-500 hover:text-indigo-400"
                      @click="orgUserGroupVisible = true">
                <div class="i-carbon-add-alt"/>
              </n-icon>
            </template>
            {{ `创建${$t('system.userGroup.orgUserGroup')}` }}
          </n-tooltip>
        </create-or-update-user-group>
      </div>
      <Transition>
        <div v-if="orgToggle">
          <div v-for="item in orgUserGroupList" :key="item.id" @click="handleListItemClick(item)">
            <create-or-update-user-group :list="orgUserGroupList"
                                         v-bind="popVisible[item.id]"
                                         @cancel="handleRenameCancel(item)"
                                         @submit="handleRenameCancel(item, item.id)">
              <div class="flex max-w-[100%] grow flex-row items-center justify-between">
                <n-tooltip>
                  <template #trigger>
                    <div class="list-item-name one-line-text">{{ item.name }}</div>
                  </template>
                  {{
                    systemType === AuthScopeEnum.ORGANIZATION
                        ? item.name +
                        `(${
                            item.internal
                                ? t('common.internal')
                                : item.scopeId === 'global'
                                    ? t('common.system.custom')
                                    : t('common.custom')
                        })`
                        : item.name
                  }}
                </n-tooltip>
                <div v-if="item.type === systemType ||
                    (isOrdShowAll &&
                      !item.internal &&
                      (item.scopeId !== 'global' || !isGlobalDisable) &&
                      orgMoreAction.length > 0)"
                     class="list-item-action flex flex-row items-center gap-[8px] opacity-0"
                     :class="{ '!opacity-100': item.id === currentId }">
                  <div v-if="item.type === systemType" class="icon-button">
                    <div v-permission="props.updatePermission" class="i-carbon-add-alt"/>
                  </div>
                  <more-action
                      v-if="isOrdShowAll && !item.internal && (item.scopeId !== 'global' || !isGlobalDisable) && orgMoreAction.length > 0"
                      :list="orgMoreAction"
                      @select="($event) => handleSelect($event, item.id,AuthScopeEnum.ORGANIZATION)"/>
                </div>
              </div>
            </create-or-update-user-group>
          </div>
          <n-divider v-if="showSystem" class="my-[0px] mt-[6px]"></n-divider>
        </div>
      </Transition>
    </div>
    <div v-if="showProject" v-permission="['PROJECT_GROUP:READ']" class="mt-2">
      <div class="flex items-center justify-between px-[4px] py-[7px]">
        <div class="flex flex-row items-center gap-1">
          <n-icon v-if="projectToggle" size="16" color="#5A5A5A" @click="projectToggle = false">
            <div class="i-ic-outline-expand-more"/>
          </n-icon>
          <n-icon v-else size="16" color="#5A5A5A" @click="projectToggle = true">
            <div class="i-ic-outline-expand-less"/>
          </n-icon>
          <div class="text-[14px]">
            {{ t('system.userGroup.projectUserGroup') }}
          </div>
        </div>
        <create-or-update-user-group :list="projectUserGroupList"
                                     :visible="projectUserGroupVisible"
                                     :auth-scope="AuthScopeEnum.PROJECT"
                                     @cancel="projectUserGroupVisible = false"
                                     @submit="handleCreateUserGroup">
          <n-tooltip>
            <template #trigger>
              <n-icon v-permission="props.addPermission" size="20" color="#DEA6A6FF"
                      @click="projectUserGroupVisible = true">
                <div class="i-carbon-add-alt"/>
              </n-icon>
            </template>
            {{ `创建${$t('system.userGroup.projectUserGroup')}` }}
          </n-tooltip>
        </create-or-update-user-group>
      </div>
      <Transition>
        <div v-if="projectToggle">
          <div v-for="item in projectUserGroupList" :key="item.id" @click="handleListItemClick(item)">
            <create-or-update-user-group :list="projectUserGroupList"
                                         v-bind="popVisible[item.id]"
                                         @cancel="handleRenameCancel(item)"
                                         @submit="handleRenameCancel(item, item.id)">
              <div class="flex max-w-[100%] grow flex-row items-center justify-between" :class="{ '!bg-green-200': item.id === currentId }">
                <div class="list-item-name one-line-text" >{{ item.name }}</div>
                <div v-if="item.type === systemType ||
                    (isProjectShowAll &&
                      !item.internal &&
                      (item.scopeId !== 'global' || !isGlobalDisable) &&
                      projectMoreAction.length > 0)"
                     class="list-item-action flex flex-row items-center gap-[8px] opacity-0"
                     :class="{ '!opacity-100': item.id === currentId }">
                  <div v-if="item.type === systemType" class="icon-button">
                    <div v-permission="props.updatePermission" class="i-carbon-add-alt" @click="handleAddMember"/>
                  </div>
                  <more-action
                      v-if="isProjectShowAll && !item.internal && (item.scopeId !== 'global' || !isGlobalDisable) && projectMoreAction.length > 0"
                      :list="projectMoreAction"
                      @select="($event) => handleSelect($event, item.id,AuthScopeEnum.PROJECT)"/>
                </div>
              </div>
            </create-or-update-user-group>
          </div>
        </div>
      </Transition>
    </div>
  </div>
  <add-user-modal ref="addUserModalRef" :visible="userModalVisible" :current-id="currentItem.id" @cancel="handleAddUserCancel"/>
</template>

<style scoped>

</style>