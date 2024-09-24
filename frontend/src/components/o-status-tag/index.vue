<script setup lang="ts">
import {planStatusType} from "/@/models/test-plan/test-plan.ts";
import {ReviewStatus} from "/@/models/case-management/case-review.ts";

const props = withDefaults(defineProps<{
  status: ReviewStatus | planStatusType;
  size?: 'small' | 'medium' | 'large';
}>(), {
  size: 'small'
});

const statusMap = {
  PREPARED: {
    label: 'caseManagement.caseReview.unStart',
    color: 'rgb(247,248,250)',
    class: '!text-[rgb(29,33,41)]',
  },
  UNDERWAY: {
    label: 'caseManagement.caseReview.going',
    color: 'rgb(190,218,255)',
    class: '!text-[rgb(var(--link-6))]',
  },
  COMPLETED: {
    label: 'caseManagement.caseReview.finished',
    color: 'rgb(175,240,181)',
    class: '!text-[rgb(var(--success-6))]',
  },
  ARCHIVED: {
    label: 'caseManagement.caseReview.archived',
    color: 'rgb(201,205,212)',
    class: '!text-[var(--color-text-4)]',
  },
  SUCCESS: {
    label: 'common.success',
    color: 'rgb(175,240,181)',
    class: '!text-[rgb(var(--success-6))]',
  },
  ERROR: {
    label: 'common.fail',
    color: 'rgb(253,205,197)',
    class: '!text-[rgb(var(--danger-6))]',
  },
  PENDING: {
    label: 'common.unExecute',
    color: 'rgb(201,205,212)',
    class: '!text-[var(--color-text-1)]',
  },
  BLOCKED: {
    label: 'common.block',
    color: '#FFF',
    class: '!text-[rgb(var(--primary-6))]',
  },
  UN_EXECUTED: {
    label: 'common.unExecute',
    color: 'rgb(201,205,212)',
    class: '!text-[var(--color-text-1)]',
  },
} as const;
</script>

<template>
  <n-tag :color="{color:statusMap[props.status]?.color}" :class="statusMap[props.status]?.class" :size="props.size">
    {{ $t(statusMap[props.status]?.label) }}
  </n-tag>
</template>

<style scoped>

</style>