<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue';
import UserGroupLeft from "/@/components/user-group-components/UserGroupLeft.vue";
import {computed, nextTick, onMounted, provide, ref, watchEffect} from "vue";
import {AuthScopeEnum} from "/@/enums/common-enum.ts";
import {useRouter} from "vue-router";
import {CurrentUserGroupItem} from "/@/models/setting/user-group.ts";
import UserTable from '/@/components/user-group-components/UserTable.vue'
import AuthTable from '/@/components/user-group-components/AuthTable.vue'
import {hasAnyPermission} from "/@/utils/permission.ts";

provide('systemType', AuthScopeEnum.SYSTEM);
const router = useRouter();
const currentKeyword = ref('');
const userGroupLeftRef = ref<InstanceType<typeof UserGroupLeft> | null>(null);
const userTableRef = ref<InstanceType<typeof UserTable> | null>(null);
const authTableRef = ref<InstanceType<typeof AuthTable> | null>(null);
const currentTable = ref('auth');
const currentUserGroupItem = ref<CurrentUserGroupItem>({
  id: '',
  name: '',
  type: AuthScopeEnum.SYSTEM,
  internal: true,
});

const couldShowUser = computed(() => currentUserGroupItem.value.type === AuthScopeEnum.SYSTEM);
const couldShowAuth = computed(() => currentUserGroupItem.value.id !== 'admin');
const handleSelect = (item: CurrentUserGroupItem) => {
  currentUserGroupItem.value = item;
};
const tableSearch = () => {
  if (currentTable.value === 'user' && userTableRef.value) {
    userTableRef.value.fetchData();
  } else if (!userTableRef.value) {
    nextTick(() => {
      userTableRef.value?.fetchData();
    });
  }
}
const handleSearch = () => {
  tableSearch()
}
const handleAddMember = (id: string) => {
  if (id === currentUserGroupItem.value.id) {
    tableSearch();
  }
}
onMounted(() => {
  userGroupLeftRef.value?.initData(router.currentRoute.value.query.id as string, true);
});
watchEffect(() => {
  if (!couldShowAuth.value) {
    currentTable.value = 'user';
  } else if (!couldShowUser.value) {
    currentTable.value = 'auth';
  } else {
    currentTable.value = 'auth';
  }
});
</script>

<template>
  <o-card simple>
    <n-split :max="0.8" :min="0.2" :default-size="0.2">
      <template #1>
        <user-group-left ref="userGroupLeftRef" :add-permission="['SYSTEM_USER_ROLE:READ+ADD']"
                         :update-permission="['SYSTEM_USER_ROLE:READ+UPDATE']"
                         :is-global-disable="false" @handle-select="handleSelect"
                         @add-user-success="handleAddMember">
        </user-group-left>
      </template>
      <template #2>
        <div class="flex h-full flex-col overflow-hidden pt-[16px]">
          <div class="mb-4 flex flex-row items-center justify-between px-[16px]">
            <n-tooltip>
              <template #trigger>
                <div class="one-line-text max-w-[300px] font-medium">{{ currentUserGroupItem.name }}</div>
              </template>
              {{ currentUserGroupItem.name }}
            </n-tooltip>
            <div class="flex items-center">
              <n-input v-if="currentTable==='user'" v-model:value="currentKeyword"
                       :placeholder="$t('system.user.searchUser')" clearable
                       @keyup.enter.prevent="handleSearch"
              />
              <n-radio-group v-if="couldShowUser && couldShowAuth" v-model:value="currentTable" class="ml-[14px]">
                <n-radio-button v-if="couldShowAuth" :value="'auth'">{{ $t('system.userGroup.auth') }}</n-radio-button>
                <n-radio-button v-if="couldShowUser" :value="'user'">{{ $t('system.userGroup.user') }}</n-radio-button>
              </n-radio-group>
            </div>
          </div>
          <div class="flex-1 overflow-hidden">
            <user-table ref="userTableRef" v-if="currentTable === 'user' && couldShowUser"
                        :keyword="currentKeyword"
                        :current="currentUserGroupItem"
                        :delete-permission="['SYSTEM_USER_ROLE:READ+DELETE']"
                        :read-permission="['SYSTEM_USER_ROLE:READ']"
                        :update-permission="['SYSTEM_USER_ROLE:READ+UPDATE']"/>
            <auth-table ref="authTableRef" v-if="currentTable === 'auth' && couldShowAuth"
                        :current="currentUserGroupItem"
                        :save-permission="['SYSTEM_USER_ROLE:READ+UPDATE']"
                        :disabled="!hasAnyPermission(['SYSTEM_USER_ROLE:READ+UPDATE'])"/>
          </div>
        </div>
      </template>
    </n-split>
  </o-card>
</template>

<style scoped>

</style>