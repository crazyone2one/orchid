<script setup lang="ts">
import {NCollapse, NCollapseItem} from "naive-ui";
</script>

<template>
  <n-collapse>
    <n-collapse-item :title="$t('common.moreSetting')" name="1">
      <div class="mt-[24px]">
        <slot name="content"></slot>
      </div>
    </n-collapse-item>
  </n-collapse>
</template>

<style scoped>

</style>