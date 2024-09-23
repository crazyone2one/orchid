<script setup lang="ts">
import ColorLine from '/@/components/o-color-line/index.vue'
import {computed} from "vue";
import {ReviewItem} from "/@/models/case-management/case-review.ts";

const props = defineProps<{
  reviewDetail: ReviewItem;
  height: string;
  radius?: string;
}>();
const colorData = computed(() => {
  if (
      props.reviewDetail.status === 'PREPARED' ||
      (props.reviewDetail.passCount === 0 &&
          props.reviewDetail.unPassCount === 0 &&
          props.reviewDetail.reReviewedCount === 0 &&
          props.reviewDetail.underReviewedCount === 0)
  ) {
    return [
      {
        percentage: 100,
        color: 'rgb(201,205,212)',
      },
    ];
  }
  return [
    {
      percentage: (props.reviewDetail.passCount / props.reviewDetail.caseCount) * 100,
      color: 'rgb(0,180,42)',
    },
    {
      percentage: (props.reviewDetail.unPassCount / props.reviewDetail.caseCount) * 100,
      color: 'rgb(245,63,63)',
    },
    {
      percentage: (props.reviewDetail.reReviewedCount / props.reviewDetail.caseCount) * 100,
      color: 'rgb(255,125,0)',
    },
    {
      percentage: (props.reviewDetail.underReviewedCount / props.reviewDetail.caseCount) * 100,
      color: 'rgb(22,93,255)',
    },
  ];
});
const progress = computed(() => {
  const result =
      ((props.reviewDetail.passCount + props.reviewDetail.unPassCount) / props.reviewDetail.caseCount) * 100;
  return `${Number.isNaN(result) ? 0 : result.toFixed(2)}%`;
});

defineExpose({
  progress,
});
</script>

<template>
  <color-line :color-data="colorData" :height="props.height" :radius="props.radius">
    <template #popoverContent>
      <table>
        <tr>
          <td class="pr-[8px] text-[rgb(201,205,212)]">{{ $t('caseManagement.caseReview.progress') }}</td>
          <td class="font-medium text-[rgb(29,33,41)]">
            {{ progress }}
            <span>
              ({{ `${props.reviewDetail.passCount + props.reviewDetail.unPassCount}/${props.reviewDetail.caseCount}` }})
            </span>
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(0,180,42)]"></div>
            <div>{{ $t('caseManagement.caseReview.pass') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.reviewDetail.passCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(245,63,63)]"></div>
            <div>{{ $t('caseManagement.caseReview.fail') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.reviewDetail.unPassCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(247,114,52)]"></div>
            <div>{{ $t('caseManagement.caseReview.reReview') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.reviewDetail.reReviewedCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(22,93,255)]"></div>
            <div>{{ $t('caseManagement.caseReview.reviewing') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.reviewDetail.underReviewedCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(201,205,212)]"></div>
            <div>{{ $t('caseManagement.caseReview.unReview') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.reviewDetail.unReviewCount }}
          </td>
        </tr>
      </table>
    </template>
  </color-line>
</template>

<style scoped>
.popover-label-td {
  @apply flex items-center;

  padding: 8px 8px 0 0;
  color: rgb(201, 205, 212);
}

.popover-value-td {
  @apply font-medium;

  padding-top: 8px;
  color: rgb(29, 33, 41);
}
</style>