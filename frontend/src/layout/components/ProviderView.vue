<template>
  <n-config-provider :locale="zhCN" :date-locale="dateZhCN">
    <slot />
  </n-config-provider>
</template>

<script setup lang="ts">
import { dateZhCN, NConfigProvider, zhCN } from "naive-ui";
</script>

<style scoped></style>
