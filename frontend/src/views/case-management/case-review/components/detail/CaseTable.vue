<script setup lang="ts">
import AdvanceFilter from '/@/components/o-advance-filter/index.vue';
import {computed, h, onActivated, onBeforeMount, ref, resolveDirective, unref, withDirectives} from "vue";
import {
  ReviewCaseItem,
  ReviewDetailCaseListQueryParams,
  ReviewPassRule, ReviewResult
} from "/@/models/case-management/case-review.ts";
import {useI18n} from "vue-i18n";
import {useAppStore, useUserStore} from "/@/store";
import {findNodeByKey} from "/@/utils";
import useCaseReviewStore from "/@/store/modules/case/case-review.ts";
import {ViewTypeEnum} from "/@/enums/advanced-filter-enum.ts";
import {DataTableColumns, DataTableRowKey, NButton, NDivider, NTooltip} from "naive-ui";
import {usePagination} from "alova/client";
import {getReviewDetailCasePage} from "/@/api/modules/case-management/case-review.ts";
import {useRoute, useRouter} from "vue-router";
import useCacheStore from "/@/store/modules/cache";
import {CaseManagementRouteEnum} from "/@/enums/route-enum.ts";
import CaseLevel from "/@/components/o-case-associate/CaseLevel.vue";
import {reviewResultMap} from "/@/config/case-management.ts";
import OButton from "/@/components/o-button/index.vue";
import PopConfirm from "/@/components/o-popconfirm/index.vue";

const props = defineProps<{
  activeFolder: string;
  reviewPassRule: ReviewPassRule; // 评审规则
  offspringIds: string[]; // 当前选中节点的所有子节点id
  reviewProgress: string; // 评审进度
}>();
const emit = defineEmits(['refresh', 'link', 'selectParentNode', 'handleAdvSearch']);
const appStore = useAppStore();
const userStore = useUserStore();
const cacheStore = useCacheStore();
const caseReviewStore = useCaseReviewStore()
const {t} = useI18n();
const route = useRoute()
const router = useRouter()
const showType = ref<'list' | 'minder'>('list');
const permission = resolveDirective('permission')
const tableParams = ref<ReviewDetailCaseListQueryParams>({
  current: 1,
  pageSize: 10,
  keyword: '',
  moduleIds: [],
  projectId: appStore.currentProjectId,
  viewFlag: false,
  reviewId: ''
});
const onlyMineStatus = ref(false);
const msAdvanceFilterRef = ref<InstanceType<typeof AdvanceFilter>>();
const isAdvancedSearchMode = computed(() => msAdvanceFilterRef.value?.isAdvancedSearchMode);
const moduleTree = computed(() => unref(caseReviewStore.moduleTree));
const moduleNamePath = computed(() => {
  return props.activeFolder === 'all'
      ? t('caseManagement.featureCase.allCase')
      : findNodeByKey<Record<string, any>>(moduleTree.value, props.activeFolder, 'id')?.name;
});
const isActivated = computed(() =>
    cacheStore.cacheViews.includes(CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL)
);
const reviewerTitlePopupVisible = ref(true);
const modulesCount = computed(() => caseReviewStore.modulesCount);
const checkedRowKeys = ref<DataTableRowKey[]>([]);
const handleCheck = (rowKeys: DataTableRowKey[]) => checkedRowKeys.value = rowKeys;
const {
  send: fetchReviewCase,
  data, page, pageSize, total
} = usePagination(() => getReviewDetailCasePage(tableParams.value), {
  immediate: false,
  initialData: {
    total: 0,
    data: []
  },
  data: response => response.records,
  total: response => response.total
});
const getStatusIcon = (status: string): Record<string, string> => {
  switch (status) {
    case "UN_REVIEWED":
      return {'icon': "i-carbon-error-filled", "color": ''}
    case "UNDER_REVIEWED":
      return {'icon': 'i-ic-baseline-autorenew', "color": 'rgb(22,93,255)'}
    case "PASS":
      return {'icon': 'i-carbon-checkmark-filled', "color": 'rgb(0,180,42)'}
    case "UN_PASS":
      return {'icon': "i-carbon-close-filled", "color": 'rgb(245,63,63)'}
    case "RE_REVIEWED":
      return {'icon': "i-carbon-recording-filled-alt", "color": 'rgb(255,125,0)'}
    default:
      return {}
  }
}
const userIsReviewer = (record: ReviewCaseItem) => {
  return record.reviewers.some((e) => e === userStore.id);
}
const columns: DataTableColumns<ReviewCaseItem> = [
  {
    type: 'selection'
  },
  {
    title: "ID",
    key: 'num',
    width: 100,
    render: (row) => h(NButton, {
      text: true, class: 'px-0 !text-[14px] !leading-[22px]',
      type: 'info',
      onClick: () => handleReview(row)
    }, {
      default: () => row.num
    })
  },
  {
    title: t('caseManagement.caseReview.caseName'),
    key: 'name',
    width: 150,
  },
  {
    title: t('caseManagement.featureCase.tableColumnLevel'),
    key: 'caseLevel',
    width: 100,
    render: (row) => h(CaseLevel, {caseLevel: row.caseLevel})
  },
  {
    title: t('caseManagement.caseReview.reviewer'),
    key: 'reviewNames',
    width: 150,
  },
  {
    title: t('caseManagement.caseReview.reviewResult'),
    key: 'status',
    width: 110,
    render: (row) => h('div', {class: 'flex items-center gap-[4px]'},
        {
          default: () => [
            h("div", {
              class: getStatusIcon(row.status)?.icon,
              style: {size: '14px', color: getStatusIcon(row.status)?.color}
            }, {}),
            h("div", {}, {default: () => t(reviewResultMap[row.status as ReviewResult].label)}),
          ]
        })
  },
  {
    title: t('caseManagement.caseReview.creator'),
    key: 'createUserName',
    width: 150,
  },
  {
    title: t('common.operation'),
    key: 'operation',
    width: 150,
    fixed: 'right',
    render: (record) => {
      return [
        h(NTooltip, {disabled: userIsReviewer(record)}, {
          trigger: () => withDirectives(h(OButton, {
            text: true,
            content: t('caseManagement.caseReview.review'),
            class: '!mr-0',
            disabled: !userIsReviewer(record),
            onClick: () => handleReview(record)
          }, {}), [[permission, ['CASE_REVIEW:READ+REVIEW']]]),
          default: () => t('caseManagement.caseReview.reviewDisabledTip')
        }),
        withDirectives(h(NDivider, {
          vertical: true,
          class: '!mx-2 h-[12px]'
        }, {}), [[permission, ['CASE_REVIEW:READ+REVIEW', 'CASE_REVIEW:READ+RELEVANCE']]]),
        h(PopConfirm, {
          title: t('caseManagement.caseReview.disassociateTip'),
          subTitleTip: t('caseManagement.caseReview.disassociateTipContent'),
          okText: t('common.confirm')
        }, {
          default: () => withDirectives(h(OButton, {
            text: true,
            content: t('caseManagement.caseReview.disassociate'),
            class: '!mr-0'
          }, {}), [[permission, ['CASE_REVIEW:READ+RELEVANCE']]]),
        })
      ]
    }
  },
]
const handleReview = (record: ReviewCaseItem) => {
  router.push({
    name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL_CASE_DETAIL,
    query: {
      ...route.query,
      caseId: record.caseId,
    },
    state: {
      params: JSON.stringify(tableParams.value),
    },
  });
}
const getModuleCount = async () => {
  if (isAdvancedSearchMode.value) return;
  let params: ReviewDetailCaseListQueryParams;
  if (showType.value === 'list') {
    params = {
      ...tableParams.value,
      current: page.value,
      pageSize: pageSize.value,
      total: total.value,
    };
  } else {
    params = {projectId: appStore.currentProjectId, pageSize: 10, current: 1};
  }
  await caseReviewStore.getModuleCount({
    ...params,
    moduleIds: [],
    reviewId: route.query.id as string,
  });
}
const searchCase = () => {
  tableParams.value = {
    ...tableParams.value,
    reviewId: route.query.id as string,
    moduleIds:
        props.activeFolder === 'all' || isAdvancedSearchMode.value ? [] : [props.activeFolder, ...props.offspringIds],
    // keyword: keyword.value,
    // viewId: viewId.value,
    // combineSearch: advanceFilter,
  };
  checkedRowKeys.value = []
  fetchReviewCase()
  getModuleCount()
}
const loadReviewCase = () => {
  searchCase();
  setTimeout(() => {
    reviewerTitlePopupVisible.value = false;
  }, 5000);
}
onBeforeMount(() => {
  if (!isActivated.value) {
    loadReviewCase();
  }
});

onActivated(() => {
  if (isActivated.value) {
    loadReviewCase();
  }
});
</script>

<template>
  <div class="h-full px-[24px] py-[16px]">
    <div class="mb-[16px]">
      <advance-filter :filter-config-list="[]"
                      :view-type="ViewTypeEnum.REVIEW_FUNCTIONAL_CASE"
                      :search-placeholder="t('caseManagement.caseReview.searchPlaceholder')"
                      :count="modulesCount[props.activeFolder] || 0"
                      :name="moduleNamePath"
                      :not-show-input-search="showType !== 'list'">
        <template v-if="showType !== 'list'" #nameRight>
          <div v-if="reviewPassRule === 'MULTIPLE'" class="ml-[16px]">
            <n-switch v-model:value="onlyMineStatus" size="small" class="mr-[4px]"/>
            {{ t('caseManagement.caseReview.myReviewStatus') }}
          </div>
          <span class="ml-[16px] !text-[rgb(var(--warning-6))]">
            {{ t('caseManagement.caseReview.cannotReviewTip') }}
          </span>
        </template>
        <template #right>
          <n-radio-group size="small" v-model:value="showType">
            <n-radio-button value="list">
              <div class="i-carbon-list !m-[2px]"/>
            </n-radio-button>
            <n-radio-button value="minder" disabled>
              <div class="i-carbon-decision-tree"/>
            </n-radio-button>
          </n-radio-group>
        </template>
      </advance-filter>
    </div>
    <template v-if="showType === 'list'">
      <n-data-table :columns="columns" :data="data" :row-key="(tmp:ReviewCaseItem)=>tmp.id"
                    @update-checked-row-keys="handleCheck"/>
    </template>
  </div>
</template>

<style scoped>

</style>