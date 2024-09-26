<script setup lang="ts">
import AdvanceFilter from '/@/components/o-advance-filter/index.vue';
import {computed, h, onActivated, onBeforeMount, ref, resolveDirective, unref, withDirectives} from "vue";
import {
  ReviewCaseItem,
  ReviewDetailCaseListQueryParams,
  ReviewPassRule,
  ReviewResult
} from "/@/models/case-management/case-review.ts";
import {useI18n} from "vue-i18n";
import {useAppStore, useUserStore} from "/@/store";
import {findNodeByKey} from "/@/utils";
import useCaseReviewStore from "/@/store/modules/case/case-review.ts";
import {FilterType, ViewTypeEnum} from "/@/enums/advanced-filter-enum.ts";
import {DataTableColumns, DataTableRowKey, NButton, NDivider, NTooltip, SelectOption} from "naive-ui";
import {usePagination, useRequest} from "alova/client";
import {getReviewDetailCasePage, getReviewUsers} from "/@/api/modules/case-management/case-review.ts";
import {useRoute, useRouter} from "vue-router";
import useCacheStore from "/@/store/modules/cache";
import {CaseManagementRouteEnum} from "/@/enums/route-enum.ts";
import CaseLevel from "/@/components/o-case-associate/CaseLevel.vue";
import {reviewResultMap} from "/@/config/case-management.ts";
import OButton from "/@/components/o-button/index.vue";
import PopConfirm from "/@/components/o-popconfirm/index.vue";
import {FilterFormItem, FilterResult} from "/@/components/o-advance-filter/type.ts";
import {
  executionResultMap,
  getReviewResultIcon
} from "/@/views/case-management/case-management-feature/components/utils.ts";
import {getCustomFieldsTable} from "/@/api/modules/case-management/feature-case.ts";
import {getFilterCustomFields} from "/@/components/o-advance-filter/index.ts";
import Pagination from "/@/components/o-pagination/index.vue";

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
              class: getReviewResultIcon(row.status)?.icon,
              style: {size: '14px', color: getReviewResultIcon(row.status)?.color}
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
const reviewersOptions = ref<SelectOption[]>([]);
const {send: fetchReviewUsers} = useRequest(() => getReviewUsers(appStore.currentProjectId, ''), {immediate: false});
const initReviewers = () => {
  fetchReviewUsers().then(res => reviewersOptions.value = res.map((e) => ({label: e.name, value: e.id})))
}
const mountedLoad = () => {
  initReviewers();
  initFilter()
}
const reviewResultOptions = computed(() => {
  return Object.keys(reviewResultMap).map((key) => {
    return {
      value: key,
      label: t(reviewResultMap[key as ReviewResult].label),
    };
  });
});
const executeResultOptions = computed(() => {
  return Object.keys(executionResultMap).map((key) => {
    return {
      value: key,
      label: executionResultMap[key].statusText,
    };
  });
});
const filterConfigList = computed<FilterFormItem[]>(() => [
  {
    title: 'caseManagement.featureCase.tableColumnID',
    dataIndex: 'num',
    type: FilterType.INPUT,
  },
  {
    title: 'caseManagement.featureCase.tableColumnName',
    dataIndex: 'name',
    type: FilterType.INPUT,
  },
  {
    title: 'common.belongModule',
    dataIndex: 'moduleId',
    type: FilterType.TREE_SELECT,
    treeSelectData: moduleTree.value,
    treeSelectProps: {
      fieldNames: {
        title: 'name',
        key: 'id',
        children: 'children',
      },
      multiple: true,
      treeCheckable: true,
      treeCheckStrictly: true,
    },
  },
  {
    title: 'caseManagement.featureCase.tableColumnReviewResult',
    dataIndex: 'status',
    type: FilterType.SELECT,
    selectProps: {
      multiple: true,
      options: reviewResultOptions.value,
    },
  },
  {
    title: 'caseManagement.caseReview.reviewer',
    dataIndex: 'reviewers',
    type: FilterType.SELECT,
    selectProps: {
      multiple: true,
      options: reviewersOptions.value,
    },
  },
  {
    title: 'caseManagement.featureCase.tableColumnExecutionResult',
    dataIndex: 'lastExecuteResult',
    type: FilterType.SELECT,
    selectProps: {
      multiple: true,
      options: executeResultOptions.value,
    },
  },
  {
    title: 'caseManagement.featureCase.associatedDemand',
    dataIndex: 'demand',
    type: FilterType.INPUT,
  },
  {
    title: 'caseManagement.featureCase.relatedAttachments',
    dataIndex: 'attachment',
    type: FilterType.INPUT,
  },
  {
    title: 'common.creator',
    dataIndex: 'createUser',
    type: FilterType.MEMBER,
  },
  {
    title: 'common.createTime',
    dataIndex: 'createTime',
    type: FilterType.DATE_PICKER,
  },
  {
    title: 'common.updateUserName',
    dataIndex: 'updateUser',
    type: FilterType.MEMBER,
  },
  {
    title: 'common.updateTime',
    dataIndex: 'updateTime',
    type: FilterType.DATE_PICKER,
  },
  {
    title: 'common.tag',
    dataIndex: 'tags',
    type: FilterType.TAGS_INPUT,
    numberProps: {
      min: 0,
      precision: 0,
    },
  },
]);
const searchCustomFields = ref<FilterFormItem[]>([]);
const {send: fetchCustomFields} = useRequest(() => getCustomFieldsTable(appStore.currentProjectId), {immediate: false})
const initFilter = () => {
  fetchCustomFields().then(res => searchCustomFields.value = getFilterCustomFields(res as any))
}
const loadReviewCase = () => {
  searchCase();
  setTimeout(() => {
    reviewerTitlePopupVisible.value = false;
  }, 5000);
}
const handleAdvSearch = (filter: FilterResult, id: string, isStartAdvance: boolean) => {
  console.log(filter)
  console.log(id)
  console.log(isStartAdvance)
  // todo 高级搜索功能
}
const handleSetPage = (param: number) => page.value = param
const handleSetPageSize = (param: number) => pageSize.value = param
onBeforeMount(() => {
  if (!isActivated.value) {
    loadReviewCase();
    mountedLoad();
  }
});

onActivated(() => {
  if (isActivated.value) {
    loadReviewCase();
    mountedLoad();
  }
});
</script>

<template>
  <div class="h-full px-[24px] py-[16px]">
    <div class="mb-[16px]">
      <advance-filter ref="advanceFilterRef" :filter-config-list="filterConfigList"
                      :custom-fields-config-list="searchCustomFields"
                      :view-type="ViewTypeEnum.REVIEW_FUNCTIONAL_CASE"
                      :search-placeholder="t('caseManagement.caseReview.searchPlaceholder')"
                      :count="modulesCount[props.activeFolder] || 0"
                      :name="moduleNamePath"
                      :not-show-input-search="showType !== 'list'"
                      @adv-search="handleAdvSearch">
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
      <div class="mt-8">
        <pagination :page-size="pageSize" :page="page" :count="total"
                    @update-page="handleSetPage"
                    @update-page-size="handleSetPageSize"/>
      </div>
    </template>
  </div>
</template>

<style scoped>

</style>