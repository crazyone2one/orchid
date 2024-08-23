<script setup lang="ts">

import {NLayoutHeader} from "naive-ui";
import TopMenu from '/@/layout/components/top-menu/index.vue'
import {useAppStore} from "/@/store";

const appStore = useAppStore()
const options = [
  {
    label: '项目1',
    value: '1'
  },
  {
    label: '项目2',
    value: '2'
  },
  {
    label: '项目3',
    value: '3'
  }
]
</script>

<template>
  <n-layout-header class="flex h-full justify-between bg-transparent" style="height: 64px; padding: 24px" bordered>
    <div class="flex w-[200px] items-center px-[16px]">
      颐和园路
    </div>
    <div class="center-side">
      <n-select v-model:value="appStore.currentProjectId" :options="options"
                class="mr-[8px] w-[200px]" filterable/>
      <top-menu/>
    </div>
  </n-layout-header>
</template>

<style scoped>
.center-side {
  @apply flex flex-1 items-center;
}
</style>