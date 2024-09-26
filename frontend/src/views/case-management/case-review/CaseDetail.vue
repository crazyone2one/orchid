<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue';
import {computed, onBeforeMount, ref, watch} from "vue";
import {
  ReviewCaseItem,
  ReviewDetailCaseListQueryParams,
  ReviewItem,
  ReviewResult
} from "/@/models/case-management/case-review.ts";
import {reviewDefaultDetail, reviewResultMap} from "/@/config/case-management.ts";
import {useI18n} from "vue-i18n";
import {useAppStore} from "/@/store";
import {useRoute, useRouter} from "vue-router";
import {usePagination, useRequest} from "alova/client";
import {getReviewDetail, getReviewDetailCasePage} from "/@/api/modules/case-management/case-review.ts";
import CheckBoxDropdown from '/@/components/o-check-box-dropdown/index.vue'
import {DetailCase} from "/@/models/case-management/feature-case.ts";
import {getCaseDetail} from "/@/api/modules/case-management/feature-case.ts";
import type {Description} from "/@/components/o-description/index.vue";
import dayjs from "dayjs";
import {
  getCustomField,
  getReviewResultIcon
} from "/@/views/case-management/case-management-feature/components/utils.ts";
import Pagination from "/@/components/o-pagination/index.vue";
import OCaseLevel from "/@/components/o-case-associate/CaseLevel.vue";
import {CaseLevel} from "/@/components/o-case-associate/types.ts";
import ReviewStatusTrigger from "/@/views/case-management/components/ReviewStatusTrigger.vue";
import ODescription from '/@/components/o-description/index.vue'
import CaseTabDetail from '/@/views/case-management/case-management-feature/components/tab-content/TabDetail.vue'

const route = useRoute();
const router = useRouter();
const appStore = useAppStore();
const {t} = useI18n();
const reviewDetail = ref<ReviewItem>({...reviewDefaultDetail});
const reviewId = ref(route.query.id as string);
const onlyMineStatus = ref(false);
const type = ref<string[]>([]);
const tableFilter = ref();
const otherListQueryParams = ref<Record<string, any>>({});
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
const caseDetail = ref<DetailCase>({...defaultCaseDetail});
const activeCaseId = ref(route.query.caseId as string);
const descriptions = ref<Description[]>([]);
const reviewStatusTriggerRef = ref<InstanceType<typeof ReviewStatusTrigger>>();
const initReviewerAndStatus = () => {
  reviewStatusTriggerRef.value?.initReviewerAndStatus(reviewId.value, activeCaseId.value);
}
const activeCaseReviewStatus = computed(() => {
  const activeCase = caseList.value.find((e) => e.caseId === activeCaseId.value);
  return onlyMineStatus.value ? activeCase?.myStatus : activeCase?.status;
});
const typeOptions = computed(() => {
  return Object.keys(reviewResultMap).map((key) => {
    return {
      value: key,
      label: t(reviewResultMap[key as ReviewResult].label),
    };
  });
});
const caseDetailLevel = computed<CaseLevel>(() => {
  if (caseDetail.value.functionalPriority) {
    return caseDetail.value.functionalPriority as CaseLevel;
  }
  return 'P1';
});
const tableParams = ref<ReviewDetailCaseListQueryParams>({
  projectId: appStore.currentProjectId,
  reviewId: reviewId.value,
  viewStatusFlag: onlyMineStatus.value,
  viewFlag: false,
  keyword: '',
  current: 1,
  pageSize: 10,
  filter: {
    ...tableFilter.value,
    status: type.value,
  },
  ...otherListQueryParams.value,
})
const {
  send: loadCaseList, loading: caseListLoading, data, page, pageSize, total
} = usePagination(() => getReviewDetailCasePage(tableParams.value), {
  immediate: false,
  initialData: {
    total: 0,
    data: []
  },
  data: response => response.records,
  total: response => response.total
});
const {send: initDetail} = useRequest(() => getReviewDetail(reviewId.value), {immediate: false, force: true});
const {
  send: fetchCaseDetail,
  loading: caseDetailLoading
} = useRequest(() => getCaseDetail(activeCaseId.value), {immediate: false, force: true});
const loadCaseDetail = () => {
  fetchCaseDetail().then(res => {
    caseDetail.value = res;
    descriptions.value = [
      {
        label: t('caseManagement.caseReview.belongModule'),
        value: res.moduleName || t('common.root'),
      },
      // 解析用例模板的自定义字段
      ...res.customFields.map((e: Record<string, any>) => {
        try {
          return {
            label: e.fieldName,
            value: getCustomField(e),
          };
        } catch (error) {
          return {
            label: e.fieldName,
            value: e.defaultValue,
          };
        }
      }),
      {
        label: t('caseManagement.caseReview.creator'),
        value: res.createUserName || '',
      },
      {
        label: t('caseManagement.caseReview.createTime'),
        value: dayjs().format('YYYY-MM-DD HH:mm:ss'),
      },
    ];
  })
}
const loadCase = async () => {
  await loadCaseList()
  loadCaseDetail();
}
const changeActiveCase = (item: ReviewCaseItem) => {
  if (activeCaseId.value !== item.caseId) {
    activeCaseId.value = item.caseId;
  }
}
const handleSetPage = (param: number) => page.value = param
const handleSetPageSize = (param: number) => pageSize.value = param
const editCaseVisible = ref(false);
const showTab = ref('detail');
const tabList = ref([
  {
    key: 'baseInfo',
    title: t('caseManagement.caseReview.caseBaseInfo'),
  },
  {
    key: 'detail',
    title: t('caseManagement.caseReview.caseDetail'),
  },
  {
    key: 'demand',
    title: t('caseManagement.caseReview.caseDemand'),
  },
  {
    key: 'reviewHistory',
    title: t('caseManagement.caseReview.reviewHistory'),
  },
]);
watch(() => activeCaseId.value, () => {
  loadCaseDetail();
  initReviewerAndStatus();
  // initReviewHistoryList()
})
onBeforeMount(async () => {
  const lastPageParams = window.history.state.params ? JSON.parse(window.history.state.params) : null;
  if (lastPageParams) {
    const {
      total,
      pageSize,
      current,
      keyword: _keyword,
      viewId,
      combineSearch,
      filter,
      sort,
      moduleIds,
    } = lastPageParams;
    // pageNation.value = {
    //   total: total || 0,
    //   pageSize,
    //   current,
    // };
    tableParams.value.keyword = _keyword;
    tableFilter.value = filter;
    // type.value = filter.status;
    otherListQueryParams.value = {
      sort,
      viewId,
      combineSearch,
      moduleIds,
    };
  } else {
    tableParams.value.keyword = route.query.reviewId as string;
  }
  await initDetail()
  await loadCase()
  initReviewerAndStatus();
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
        <n-spin :show="caseListLoading" class="h-[calc(100%-46px)] w-full">
          <div class="case-list">
            <div
                v-for="item of data"
                :key="item.caseId"
                :class="['case-item', caseDetail.id === item.caseId ? 'case-item--active' : '']"
                @click="changeActiveCase(item)"
            >
              <div class="mb-[4px] flex items-center justify-between">
                <div>{{ item.num }}</div>
                <div v-if="onlyMineStatus" class="flex items-center gap-[4px] leading-[22px]">
                  <div :class="getReviewResultIcon(item.myStatus).icon"
                       :style="{color:getReviewResultIcon(item.myStatus).color}"/>
                  {{ t(reviewResultMap[item.myStatus]?.label) }}
                </div>
                <div v-else class="flex items-center gap-[4px] leading-[22px]">
                  <div :class="getReviewResultIcon(item.status).icon"
                       :style="{color:getReviewResultIcon(item.status).color}"/>
                  {{ t(reviewResultMap[item.status]?.label) }}
                </div>
              </div>
              <div class="one-line-text">{{ item.name }}</div>
            </div>
            <n-empty v-if="data.length === 0" :description="t('common.noData')"/>
          </div>
          <div class="mt-8">
            <pagination :page-size="pageSize" :page="page" :count="total"
                        @update-page="handleSetPage"
                        @update-page-size="handleSetPageSize"/>
          </div>
        </n-spin>
      </div>
      <n-spin :show="caseDetailLoading" class="relative flex flex-1 flex-col overflow-hidden">
        <n-empty v-if="!data.length" :description="t('common.noData')"/>
        <template v-else>
          <div class="content-center">
            <div class="rounded p-[16px]">
              <div class="mb-[12px] flex items-center">
                <div class="mr-[16px] flex-1 overflow-hidden">
                  <n-tooltip>
                    <template #trigger>
                      <div
                          class="one-line-text w-[fit-content] max-w-[100%] cursor-pointer font-medium "
                          @click="goCaseDetail"
                      >
                        【{{ caseDetail.num }}】{{ caseDetail.name }}
                      </div>
                    </template>
                    {{ `【${caseDetail.num}】${caseDetail.name}` }}
                  </n-tooltip>
                </div>
                <n-button v-permission="['FUNCTIONAL_CASE:READ+UPDATE']" size="small" @click="editCaseVisible = true">
                  {{ t('common.edit') }}
                </n-button>
              </div>
              <div class="flex items-center">
                <div class="i-ic-baseline-folder mr-[4px] text-slate-200 size-5"/>
                <n-tooltip>
                  <template #trigger>
                    <div class="one-line-text mr-[8px] max-w-[300px] font-medium">
                      {{ caseDetail.moduleName || t('common.root') }}
                    </div>
                  </template>
                  {{ caseDetail.moduleName || t('common.root') }}
                </n-tooltip>
                <div class="case-detail-label">
                  {{ t('caseManagement.caseReview.caseLevel') }}
                </div>
                <div class="case-detail-value">
                  <o-case-level :case-level="caseDetailLevel"/>
                </div>
                <div class="case-detail-label">
                  {{ t('caseManagement.caseReview.reviewResult') }}
                </div>
                <div class="case-detail-value">
                  <ReviewStatusTrigger v-if="reviewDetail.reviewPassRule === 'MULTIPLE'" ref="reviewStatusTriggerRef"/>
                  <div
                      v-if="reviewResultMap[activeCaseReviewStatus as ReviewResult] && reviewDetail.reviewPassRule !== 'MULTIPLE'"
                      class="flex items-center gap-[4px]">
                    <div :class="getReviewResultIcon(activeCaseReviewStatus as ReviewResult).icon"/>
                    {{ t(reviewResultMap[activeCaseReviewStatus as ReviewResult].label) }}
                  </div>
                </div>
              </div>
            </div>
            <n-tabs v-model:value="showTab" type="line" animated>
              <n-tab-pane :name="tabList[0].key" :tab="tabList[0].title"/>
              <n-tab-pane :name="tabList[1].key" :tab="tabList[1].title"/>
              <n-tab-pane :name="tabList[2].key">
                <template #default>
                  <div class="flex items-center">
                    {{ tabList[2].title }}
                    <div
                        v-if="caseDetail.demandCount > 0"
                        :class="`ml-[4px] flex h-[16px] min-w-[16px] items-center justify-center rounded-full ${
                        showTab === tabList[2].key
                          ? 'bg-[rgb(var(--primary-9))] text-[rgb(var(--primary-5))]'
                          : 'bg-[var(--color-text-brand)] text-white'
                      } px-[4px] text-[12px]`"
                    >
                      {{ caseDetail.demandCount > 99 ? '99+' : caseDetail.demandCount }}
                    </div>
                  </div>
                </template>
              </n-tab-pane>
              <n-tab-pane :name="tabList[3].key" :tab="tabList[3].title"/>
            </n-tabs>
            <n-divider class="my-0"/>
            <o-description v-if="showTab==='baseInfo'" :descriptions="descriptions" label-width="90px"
                           class="mt-[16px]"/>
            <div v-else-if="showTab === 'detail'" class="mt-[16px] h-full">
              <caseTabDetail :form="caseDetail" :allow-edit="false"/>
            </div>
            <div v-else-if="showTab === 'demand'">
              <div class="mt-[16px] flex items-center justify-between">
                {{ t('caseManagement.caseReview.demandCases') }}
                <div>
                  <n-input :placeholder="t('caseManagement.caseReview.demandSearchPlaceholder')" class="w-[300px]"/>
                </div>
              </div>
              todo...
            </div>
            <div v-else class="flex flex-1 flex-col overflow-hidden pl-[16px] pt-[16px]">
              <div class="ms-comment-list">
              </div>
            </div>
          </div>
        </template>
      </n-spin>
    </div>
  </o-card>
</template>

<style scoped>
.case-list {
  overflow-y: auto;
  margin-bottom: 12px;
  padding: 16px;
  height: calc(100% - 60px);

  .case-item {
    @apply cursor-pointer;

    &:not(:last-child) {
      margin-bottom: 8px;
    }

    padding: 12px;
  }

  .case-item--active {
    @apply relative;

    border: 1px solid rgb(64, 128, 255);
    background-color: rgb(232, 243, 255);
  }
}

.case-detail-label {
  margin-right: 8px;
  color: rgb(201, 205, 212);
}

.case-detail-value {
  @apply flex items-center;

  margin-right: 16px;
}
</style>