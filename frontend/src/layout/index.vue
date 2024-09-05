<script setup lang="ts">
import {NLayout, NLayoutFooter,} from "naive-ui";
import NavBar from '/@/layout/components/navbar/index.vue'
import sideBar from '/@/layout/components/sidebar/index.vue'
import {useAppStore} from "/@/store";
import OBreadCrumb from '/@/components/o-breadcrumb/index.vue'

const appStore = useAppStore()

</script>
<template>
  <n-layout :style="{ height: appStore.innerHeight + 'px' }">
    <nav-bar/>
    <n-layout position="absolute" style="top: 64px; bottom: 64px" has-sider>
      <side-bar/>
      <n-layout content-style="padding: 24px;" :native-scrollbar="false">
        <o-bread-crumb/>
        <router-view/>
      </n-layout>
    </n-layout>
    <n-layout-footer
        position="absolute"
        style="height: 64px; padding: 24px"
        bordered
    >
      城府路
    </n-layout-footer>
  </n-layout>
</template>

<style scoped></style>
