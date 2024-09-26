<script setup lang="ts">
import {ReviewResult} from "/@/models/case-management/case-review.ts";
import {computed} from "vue";
import {reviewResultMap} from "/@/config/case-management.ts";
import {getReviewResultIcon} from "/@/views/case-management/case-management-feature/components/utils.ts";

const props = withDefaults(
    defineProps<{
      status?: ReviewResult;
      isPart?: boolean; // 为true时，'UNDER_REVIEWED'字段对应的是'建议'
      iconSize?: number;
    }>(),
    {
      iconSize: 16,
    }
);
const resultMap = computed(() =>
    props.isPart
        ? {
          ...reviewResultMap,
          ...{
            PASS: {
              label: 'common.pass',
              color: 'rgb(var(--success-6))',
              icon: 'icon-icon_succeed_filled',
            },
            UNDER_REVIEWED: {
              label: 'caseManagement.caseReview.suggestion',
              color: 'rgb(var(--warning-6))',
              icon: 'icon-icon_warning_filled',
            },
          },
        }
        : reviewResultMap
);
</script>

<template>
  <div v-if="props.status" class="flex items-center">
    <div :class="getReviewResultIcon(props.status).icon || ''" class="mr-[4px] "
         :style="{color:getReviewResultIcon(props.status).color}"/>
    {{ $t(resultMap[props.status]?.label) }}
  </div>
</template>

<style scoped>

</style>