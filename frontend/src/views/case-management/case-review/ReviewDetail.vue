<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue';
import {CaseManagementRouteEnum} from "/@/enums/route-enum.ts";
import {useRoute, useRouter} from "vue-router";
import {computed, onMounted, ref} from "vue";
import {useRequest} from "alova/client";
import {getReviewDetail} from "/@/api/modules/case-management/case-review.ts";
import {reviewDefaultDetail} from "/@/config/case-management.ts";
import {ReviewItem, ReviewStatus} from "/@/models/case-management/case-review.ts";
import StatusTag from '/@/components/o-status-tag/index.vue'
import PassRateLine from "/@/views/case-management/case-review/components/PassRateLine.vue";
import SplitBox from '/@/components/o-split-box/index.vue'
import CaseTree from "/@/views/case-management/case-review/components/detail/CaseTree.vue";
import useCaseReviewStore from "/@/store/modules/case/case-review.ts";
import CaseTable from "/@/views/case-management/case-review/components/detail/CaseTable.vue";
import {useAppStore} from "/@/store";
import {ModuleTreeNode} from "/@/models/common.ts";

const router = useRouter();
const route = useRoute();
const appStore = useAppStore()
const caseReviewStore = useCaseReviewStore()
const reviewId = ref(route.query.id as string);
const reviewDetail = ref<ReviewItem>({
  ...reviewDefaultDetail,
});
const isAdvancedSearchMode = ref(false);
const passRateLineRef = ref<InstanceType<typeof PassRateLine>>();
const folderTreeRef = ref<InstanceType<typeof CaseTree>>();
const caseTableRef = ref<InstanceType<typeof CaseTable>>();
const activeFolderId = ref<string>('all');
const offspringIds = ref<string[]>([]);
const modulesCount = computed(() => caseReviewStore.modulesCount);
const selectedKeys = computed({
  get: () => [activeFolderId.value],
  set: (val) => val,
});
const reviewProgress = computed(() => passRateLineRef.value?.progress ?? '');
const associateDrawerVisible = ref(false);
const associateDrawerProject = ref(appStore.currentProjectId);
const {send: fetchReviewDetail} = useRequest(() => getReviewDetail(reviewId.value), {immediate: false, force: true});
const initDetail = () => {
  fetchReviewDetail().then(res => reviewDetail.value = res)
}
const backToTable = () => {
  router.push({
    name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW,
  });
}
const copyReview = () => {
  router.push({
    name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_CREATE,
    query: {
      copyId: reviewId.value,
    },
  });
}
const createCase = () => {
  router.push({
    name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
    query: {reviewId: reviewId.value,},
  });
}
const editReview = () => {
  router.push({
    name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_CREATE,
    query: {id: reviewId.value,},
  });
}
const handleFolderNodeSelect = (ids: string[], _offspringIds: string[]) => {
  [activeFolderId.value] = ids;
  offspringIds.value = [..._offspringIds];
}
const selectParentNode = (folderTree: ModuleTreeNode[]) => {
  folderTreeRef.value?.selectParentNode(folderTree);
}
const handleAdvSearch = (isStartAdvance: boolean) => {
  isAdvancedSearchMode.value = isStartAdvance;
  folderTreeRef.value?.setActiveFolder('all');
}
onMounted(() => {
  initDetail();
});
</script>

<template>
  <o-card hide-footer :header-min-width="1100" :handle-back="backToTable">
    <template #headerLeft>
      <n-tooltip>
        <template #trigger>
          <div class="one-line-text mr-[8px] max-w-[300px] font-medium text-[var(--color-text-000)]">
            {{ reviewDetail.name }}
          </div>
        </template>
        {{ reviewDetail.name }}
      </n-tooltip>
      <div
          class="rounded-[0_999px_999px_0] border border-solid border-[text-[rgb(var(--primary-5))]] px-[8px] py-[2px] text-[12px] leading-[16px] text-blue-600">
        <div class="i-carbon-flow-logs-vpc"/>
        {{
          reviewDetail.reviewPassRule === 'SINGLE' ? $t('caseManagement.caseReview.single') : $t('caseManagement.caseReview.multi')
        }}
      </div>
      <status-tag :status="(reviewDetail.status as ReviewStatus)" class="mx-[16px]"/>
    </template>
    <template #headerRight>
      <n-button v-permission="['CASE_REVIEW:READ+UPDATE']" size="small" secondary>
        <div class="i-carbon-link mr-[8px]"/>
        {{ $t('ms.case.associate.title') }}
      </n-button>
      <n-button v-permission="['CASE_REVIEW:READ+UPDATE']" size="small" secondary>
        <div class="i-carbon-edit mr-[8px]"/>
        {{ $t('common.edit') }}
      </n-button>
      <n-button v-permission="['CASE_REVIEW:READ+ADD']" size="small" secondary>
        <div class="i-carbon-copy mr-[8px]"/>
        {{ $t('common.copy') }}
      </n-button>
      <n-button v-permission="['CASE_REVIEW:READ+UPDATE']" size="small" secondary>
        <div :class="reviewDetail.followFlag?'i-carbon-star-filled text-orange':'i-carbon-star'" class="mr-[8px]"/>
        {{ $t(reviewDetail.followFlag ? 'common.forked' : 'common.fork') }}
      </n-button>
    </template>
    <template #subHeader>
      <div class="mt-[1px] w-[476px]">
        <div class="mb-[4px] flex items-center gap-[24px]">
          <div class="text-[rgb(201,205,212)]">
            <span class="mr-[8px]">{{ $t('caseManagement.caseReview.reviewedCase') }}</span>
            <span v-if="reviewDetail.status === 'PREPARED'">-</span>
            <span v-else>
              <span class="text-black"> {{ reviewDetail.reviewedCount }}/ </span>{{ reviewDetail.caseCount }}
            </span>
          </div>
          <div class="text-[rgb(201,205,212)]">
            <span class="mr-[8px]">{{ $t('caseManagement.caseReview.passRate') }}</span>
            <span v-if="reviewDetail.status === 'PREPARED'">-</span>
            <span v-else>
              <span class="text-black"> {{ reviewDetail.passRate }}% </span>
            </span>
          </div>
        </div>
        <pass-rate-line ref="passRateLineRef" :review-detail="reviewDetail" height="8px" radius="2px"/>
      </div>
    </template>
    <split-box :not-show-first="isAdvancedSearchMode">
      <template #first>
        <div class="p-[16px]">
          <case-tree ref="folderTreeRef" :modules-count="modulesCount"
                     :selected-keys="selectedKeys"
                     @folder-node-select="handleFolderNodeSelect"/>
        </div>
      </template>
      <template #second>
        <case-table ref="caseTableRef" :active-folder="activeFolderId"
                    :review-pass-rule="reviewDetail.reviewPassRule"
                    :offspring-ids="offspringIds"
                    :modules-count="modulesCount"
                    :review-progress="reviewProgress"
                    @refresh="initDetail()"
                    @link="associateDrawerVisible = true"
                    @select-parent-node="selectParentNode"
                    @handle-adv-search="handleAdvSearch"/>
      </template>
    </split-box>
  </o-card>
</template>

<style scoped>
.n-button {
  &:not(:last-child) {
    @apply mr-2;
  }

  padding: 0 4px;
}
</style>