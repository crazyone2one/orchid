<script setup lang="ts">
import {DetailCase} from "/@/models/case-management/feature-case.ts";
import {ref} from "vue";

const props = defineProps<{
  detail: DetailCase;
}>();

const emit = defineEmits<{
  (e: 'updateSuccess'): void;
}>();
const loading = ref<boolean>(false);
</script>

<template>
  <n-spin :show="loading" class="w-full">
    <div class="h-full w-full">
      <div class="basic-container">
        <div class="baseItem">
          <span class="label"> {{ $t('caseManagement.featureCase.tableColumnModule') }}</span>
          <span class="w-full">
          </span>
        </div>
      </div>
    </div>
  </n-spin>
</template>

<style scoped>

</style>