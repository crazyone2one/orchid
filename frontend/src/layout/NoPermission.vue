<script setup lang="ts">
import {NButton, NResult} from "naive-ui";
</script>

<template>
  <n-result
      status="403"
      title="403 禁止访问"
      description="总有些门是对你关闭的"
  >
    <template #footer>
      <n-button>放轻松</n-button>
    </template>
  </n-result>
</template>

<style scoped>

</style>