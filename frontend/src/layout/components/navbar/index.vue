<script setup lang="ts">

import {NLayoutHeader} from "naive-ui";
import TopMenu from '/@/layout/components/top-menu/index.vue'
import PersonalInfoMenu from '/@/layout/components/personal-info-menu/index.vue'
import {useAppStore} from "/@/store";
import {ref, watch} from "vue";
import {hasAnyPermission} from "/@/utils/permission.ts";
import AddProjectModal from "/@/views/setting/system/organizationAndProject/components/AddProjectModal.vue";

const appStore = useAppStore()
const projectVisible = ref(false);
watch(() => appStore.currentOrgId, () => {
  appStore.initProjectList()
}, {immediate: true})
</script>

<template>
  <n-layout-header class="flex h-full justify-between bg-transparent" style="height: 64px; padding: 24px" bordered>
    <div class="flex w-[200px] items-center px-[16px]">
      颐和园路
    </div>
    <div class="center-side">
      <n-select v-model:value="appStore.currentProjectId" :options="appStore.projectList"
                label-field="name" value-field="id"
                class="mr-[8px] w-[200px]" filterable>
        <template #arrow>
          <n-icon size="16" color="#D33B3BFF">
            <div class="i-carbon-caret-down"/>
          </n-icon>
        </template>
        <template v-if="hasAnyPermission(['ORGANIZATION_PROJECT:READ+ADD'])" #header>
          <n-button type="info" class="mb-[4px] h-[28px] w-full justify-start pl-[7px] pr-0" text
                    @click="projectVisible=true">
            <template #icon>
              <div class="i-carbon-add-alt"/>
            </template>
            {{ $t('settings.navbar.createProject') }}
          </n-button>
        </template>
      </n-select>
      <top-menu/>
    </div>
    <div class="flex items-center">
      <personal-info-menu/>
    </div>
  </n-layout-header>
  <add-project-modal :visible="projectVisible" @cancel="projectVisible = false"/>
</template>

<style scoped>
.center-side {
  @apply flex flex-1 items-center;
}
</style>