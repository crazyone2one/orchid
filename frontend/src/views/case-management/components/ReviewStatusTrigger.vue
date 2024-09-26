<script setup lang="ts">
import {ref} from "vue";
import ReviewResult from "/@/views/case-management/case-review/components/ReviewResult.vue";
import {ReviewResult as ReviewResultStatus} from '/@/models/case-management/case-review.ts';
import {getReviewerAndStatus} from "/@/api/modules/case-management/case-review.ts";
import {OptionItem} from "/@/api/modules/message";

const props = defineProps<{
  size?: number;
}>();
const statusVisible = ref(false);
const reviewHistoryStatus = ref<ReviewResultStatus>();
const reviewUserStatusList = ref<OptionItem[]>([]); // 每个评审人最后一次评审结果
const initReviewerAndStatus = async (reviewId: string, caseId: string) => {
  console.log('initReviewerAndStatus')
  const res = await getReviewerAndStatus(reviewId, caseId);
  reviewUserStatusList.value = res.reviewerStatus;
  reviewHistoryStatus.value = res.status as ReviewResultStatus;
}
defineExpose({
  initReviewerAndStatus,
});
</script>

<template>
  <n-popover v-model:show="statusVisible" trigger="click"
             content-class="review-result-trigger-content">
    <template #trigger>
      <div :class="`overall-review-result flex cursor-pointer items-center rounded p-[4px]`">
        <ReviewResult :status="reviewHistoryStatus" :icon-size="props.size"/>
        <div class=" i-ic-baseline-expand-circle-down ml-[4px] size-4"/>
      </div>
    </template>
    <div v-for="item in reviewUserStatusList" :key="item.id" class="my-[4px] flex justify-between">
      <div class="one-line-text max-w-[92px] mr-2">
        {{ item.id }}
      </div>
      <ReviewResult :status="item.name as ReviewResultStatus" :icon-size="props.size"/>
    </div>
    <n-empty v-if="!reviewUserStatusList.length" />
  </n-popover>
</template>

<style scoped>
.review-result-trigger-content {
  padding: 6px;
  width: 150px;
  max-height: 192px;
  border-radius: 4px;
  box-shadow: 0 -1px 4px rgb(2 2 2 / 10%);
  @apply overflow-y-auto overflow-x-hidden bg-white;
}

.overall-review-result {
  padding: 0;
}
</style>