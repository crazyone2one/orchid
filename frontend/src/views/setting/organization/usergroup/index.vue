<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue'
import UserGroupLeft from "/@/components/user-group-components/UserGroupLeft.vue";
import {AuthScopeEnum} from "/@/enums/common-enum.ts";
import {computed, nextTick, onMounted, provide, ref, watchEffect} from "vue";
import {useRouter} from "vue-router";
import {CurrentUserGroupItem} from "/@/models/setting/user-group.ts";
import AuthTable from "/@/components/user-group-components/AuthTable.vue";
import UserTable from "/@/components/user-group-components/UserTable.vue";
import {hasAnyPermission} from "/@/utils/permission.ts";

// 注入系统层级
provide('systemType', AuthScopeEnum.ORGANIZATION);
const router = useRouter();
const ugLeftRef = ref();
const currentTable = ref('user');
const currentUserGroupItem = ref<CurrentUserGroupItem>({
  id: '',
  name: '',
  type: AuthScopeEnum.ORGANIZATION,
  internal: true,
});
const couldShowUser = computed(() => currentUserGroupItem.value.type === AuthScopeEnum.ORGANIZATION);
const couldShowAuth = computed(() => currentUserGroupItem.value.id !== 'admin');
const userRef = ref();
const handleSelect = (item: CurrentUserGroupItem) => {
  currentUserGroupItem.value = item;
};
const tableSearch = () => {
  if (currentTable.value === 'user' && userRef.value) {
    userRef.value.fetchData();
  } else if (!userRef.value) {
    nextTick(() => {
      userRef.value?.fetchData();
    });
  }
};
const handleAddMember = (id: string) => {
  if (id === currentUserGroupItem.value.id) {
    tableSearch();
  }
}
watchEffect(() => {
  if (!couldShowAuth.value) {
    currentTable.value = 'user';
  } else if (!couldShowUser.value) {
    currentTable.value = 'auth';
  } else {
    currentTable.value = 'auth';
  }
});
onMounted(() => {
  ugLeftRef.value?.initData(router.currentRoute.value.query.id, true);
});
</script>

<template>
  <o-card simple>
    <n-split :max="0.8" :min="0.2" :default-size="0.2">
      <template #1>
        <div class="mr-1">
          <user-group-left ref="ugLeftRef"
                           :add-permission="['ORGANIZATION_USER_ROLE:READ+ADD']"
                           :update-permission="['ORGANIZATION_USER_ROLE:READ+UPDATE']"
                           :is-global-disable="true"
                           @handle-select="handleSelect"
                           @add-user-success="handleAddMember"/>
        </div>
      </template>
      <template #2>
        <div class="p-[16px]">
          <div class="flex flex-row items-center justify-between">
            <n-radio-group v-if="couldShowUser && couldShowAuth" v-model:value="currentTable" class="ml-[14px]">
              <n-radio-button v-if="couldShowAuth" :value="'auth'">{{ $t('system.userGroup.auth') }}</n-radio-button>
              <n-radio-button v-if="couldShowUser" :value="'user'">{{ $t('system.userGroup.user') }}</n-radio-button>
            </n-radio-group>
            <div class="flex items-center">
              <n-input v-if="currentTable === 'user'" :placeholder="$t('system.user.searchUser')" class="w-[240px]"
                       clearable/>
            </div>
          </div>
          <div class="mt-[16px]">
            <auth-table v-if="currentTable === 'auth' && couldShowAuth" :current="currentUserGroupItem"
                        :save-permission="['ORGANIZATION_USER_ROLE:READ+UPDATE']"
                        :disabled="
                !hasAnyPermission(['ORGANIZATION_USER_ROLE:READ+UPDATE']) || currentUserGroupItem.scopeId === 'global'
              "/>
            <user-table ref="userRef" v-if="currentTable === 'user' && couldShowUser" keyword=""
                        :current="currentUserGroupItem"
                        :delete-permission="['ORGANIZATION_USER_ROLE:READ+DELETE']"
                        :read-permission="['ORGANIZATION_USER_ROLE:READ']"
                        :update-permission="['ORGANIZATION_USER_ROLE:READ+UPDATE']"/>
          </div>
        </div>
      </template>
    </n-split>
  </o-card>
</template>

<style scoped>

</style>