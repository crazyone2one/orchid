<script setup lang="ts">
import {useWindowSize} from "@vueuse/core";
import {
  NH2,
  NLayout,
  NLayoutFooter,
  NLayoutHeader,
  NLayoutSider,
  NMenu,
} from "naive-ui";
import {ref} from "vue";

const menuOptions = [
  {
    label: "且听风吟",
    key: "hear-the-wind-sing",
  },
  {
    label: "1973年的弹珠玩具",
    key: "pinball-1973",

    disabled: true,
    children: [
      {
        label: "鼠",
        key: "rat",
      },
    ],
  },
];
const inverted = ref(false);
const {height} = useWindowSize();
</script>
<template>
  <n-layout :style="{ height: height + 'px' }">
    <n-layout-header style="height: 64px; padding: 24px" bordered>
      颐和园路
    </n-layout-header>
    <n-layout position="absolute" style="top: 64px; bottom: 64px" has-sider>
      <n-layout-sider
          content-style="padding: 24px;"
          show-trigger
          collapse-mode="width"
          :collapsed-width="64"
          :width="240"
          :native-scrollbar="false"
          bordered
      >
        <n-menu
            :inverted="inverted"
            :collapsed-width="64"
            :collapsed-icon-size="22"
            :options="menuOptions"
        />
      </n-layout-sider>
      <n-layout content-style="padding: 24px;" :native-scrollbar="false">
        <n-h2>平山道</n-h2>
        <n-h2>平山道</n-h2>
        <n-h2>平山道</n-h2>
        <n-h2>平山道</n-h2>
        <n-h2>平山道</n-h2>
        <n-h2>平山道</n-h2>
        <n-h2>平山道</n-h2>
        <n-h2>平山道</n-h2>
        <n-h2>平山道</n-h2>
        <n-h2>平山道</n-h2>
        <n-h2>平山道</n-h2>
        <n-h2>平山道</n-h2>
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
