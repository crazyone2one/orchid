<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue';
import {computed, onBeforeMount, ref} from "vue";
import {ReviewCaseItem, ReviewItem, ReviewResult} from "/@/models/case-management/case-review.ts";
import {reviewDefaultDetail, reviewResultMap} from "/@/config/case-management.ts";
import {useI18n} from "vue-i18n";
import {useAppStore} from "/@/store";
import {useRoute, useRouter} from "vue-router";
import {useRequest} from "alova/client";
import {getReviewDetail} from "/@/api/modules/case-management/case-review.ts";
import CheckBoxDropdown from '/@/components/o-check-box-dropdown/index.vue'
import {DetailCase} from "/@/models/case-management/feature-case.ts";

const route = useRoute();
const router = useRouter();
const appStore = useAppStore();
const {t} = useI18n();
const reviewDetail = ref<ReviewItem>({...reviewDefaultDetail});
const reviewId = ref(route.query.id as string);
const onlyMineStatus = ref(false);
const type = ref<string[]>([]);
const caseList = ref<ReviewCaseItem[]>([]);
const defaultCaseDetail: DetailCase = {
  id: '',
  projectId: '',
  templateId: '',
  name: '',
  prerequisite: '', // prerequisite
  caseEditType: '', // 编辑模式：步骤模式/文本模式
  steps: '',
  textDescription: '',
  expectedResult: '', // 预期结果
  description: '',
  publicCase: false, // 是否公共用例
  moduleId: '',
  versionId: '',
  tags: [],
  customFields: [], // 自定义字段集合
  relateFileMetaIds: [], // 关联文件ID集合
  reviewStatus: 'UN_REVIEWED',
  functionalPriority: '',
};
const caseDetail = ref<DetailCase>({ ...defaultCaseDetail });
const typeOptions = computed(() => {
  return Object.keys(reviewResultMap).map((key) => {
    return {
      value: key,
      label: t(reviewResultMap[key as ReviewResult].label),
    };
  });
});
const {send: initDetail} = useRequest(() => getReviewDetail(reviewId.value), {immediate: false, force: true})
onBeforeMount(() => {
  const lastPageParams = window.history.state.params ? JSON.parse(window.history.state.params) : null;
  console.log('lastPageParams',lastPageParams)
  initDetail().then(res => reviewDetail.value = res)
});
</script>

<template>
  <o-card :min-width="1100" has-breadcrumb hide-footer hide-divider>
    <template #headerLeft>
      <div class="one-line-text mr-[8px] max-w-[300px] font-medium">
        {{ reviewDetail.name }}
      </div>
      <div
          class="rounded-[0_999px_999px_0] border border-solid  px-[8px] py-[2px] text-[12px] leading-[16px] text-blue-600"
      >
        <div class="i-carbon-flow-logs-vpc"/>
        {{
          reviewDetail.reviewPassRule === 'SINGLE' ? t('caseManagement.caseReview.single') : t('caseManagement.caseReview.multi')
        }}
      </div>
      <div v-show="reviewDetail.reviewPassRule === 'MULTIPLE'" class="ml-[16px] flex items-center">
        <n-switch v-model:value="onlyMineStatus" size="small" class="mr-[8px]" type="line"/>
        {{ t('caseManagement.caseReview.myReviewStatus') }}
      </div>
    </template>
    <div class="flex h-full w-full border-t">
      <div class="h-full w-[356px] border-r py-[16px] pl-[24px] pr-[16px]">
        <div class="mb-[16px] flex">
          <n-input :placeholder="t('caseManagement.caseReview.searchPlaceholder')" class="mr-[8px] flex-1"/>
          <check-box-dropdown
              v-model:select-list="type"
              :title="t('caseManagement.featureCase.reviewResult')" :options="typeOptions"
              :disabled="onlyMineStatus">

          </check-box-dropdown>
        </div>
        <n-spin class="h-[calc(100%-46px)] w-full">
          <div class="case-list">
            <div
                v-for="item of caseList"
                :key="item.caseId"
                :class="['case-item', caseDetail.id === item.caseId ? 'case-item--active' : '']"
                @click="changeActiveCase(item)"
            >
              <div class="mb-[4px] flex items-center justify-between">
                <div>{{ item.num }}</div>
                <div v-if="onlyMineStatus" class="flex items-center gap-[4px] leading-[22px]">
<!--                  <MsIcon-->
<!--                      :type="reviewResultMap[item.myStatus]?.icon ?? ''"-->
<!--                      :style="{ color: reviewResultMap[item.myStatus]?.color }"-->
<!--                  />-->
                  {{ t(reviewResultMap[item.myStatus]?.label) }}
                </div>
                <div v-else class="flex items-center gap-[4px] leading-[22px]">
<!--                  <MsIcon-->
<!--                      :type="reviewResultMap[item.status]?.icon"-->
<!--                      :style="{ color: reviewResultMap[item.status]?.color }"-->
<!--                  />-->
                  {{ t(reviewResultMap[item.status]?.label) }}
                </div>
              </div>
              <n-tooltip :content="item.name">
                <div class="one-line-text">{{ item.name }}</div>
              </n-tooltip>
            </div>
          </div>
        </n-spin>
      </div>
      <n-spin class="relative flex flex-1 flex-col overflow-hidden">

      </n-spin>
    </div>
  </o-card>
</template>

<style scoped>

</style>