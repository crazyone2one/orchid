<script setup lang="ts">
import {ModuleTreeNode} from "/@/models/common.ts";
import {ReviewItem, ReviewListQueryParams, ReviewStatus} from "/@/models/case-management/case-review.ts";
import {useVModel} from "@vueuse/core";
import AdvanceFilter from '/@/components/o-advance-filter/index.vue'
import {useI18n} from "vue-i18n";
import {FilterFormItem} from "/@/components/o-advance-filter/type.ts";
import {h, onBeforeMount, ref, resolveDirective, watch, withDirectives} from "vue";
import {useAppStore, useUserStore} from "/@/store";
import {DataTableColumns, DataTableRowKey, type DropdownOption, NButton, NDivider, NTag, NTooltip} from "naive-ui";
import {usePagination} from "alova/client";
import {getReviewList} from "/@/api/modules/case-management/case-review.ts";
import StatusTag from '/@/components/o-status-tag/index.vue'
import PassRateLine from "/@/views/case-management/case-review/components/PassRateLine.vue";
import TagGroup from '/@/components/o-tag-group/index.vue'
import OButton from "/@/components/o-button/index.vue";
import TableMoreAction from '/@/components/o-table-more-action/index.vue'
import {hasAllPermission} from "../../../../../utils/permission.ts";

const props = defineProps<{
  activeFolder: string;
  moduleTree: ModuleTreeNode[];
  treePathMap: Record<
      string,
      {
        path: string;
        fullPath: string;
      }
  >;
  showType: string;
  offspringIds: string[];
}>();

const emit = defineEmits<{
  (e: 'goCreate'): void;
  (e: 'update:showType'): void;
  (e: 'init', params: ReviewListQueryParams): void;
}>();
const {t} = useI18n()
const appStore = useAppStore()
const userStore = useUserStore()
const filterConfigList = ref<FilterFormItem[]>([]);
const innerShowType = useVModel(props, 'showType', emit);
const reqParam = ref<ReviewListQueryParams>({
  current: 1,
  pageSize: 10,
  keyword: '', moduleIds: [], projectId: appStore.currentProjectId
})
const activeRecord = ref({
  id: '',
  name: '',
  status: 'PREPARED' as ReviewStatus,
});
const confirmReviewName = ref('');
const permission = resolveDirective('permission')
const checkedRowKeys = ref<DataTableRowKey[]>([]);
const handleCheck = (rowKeys: DataTableRowKey[]) => checkedRowKeys.value = rowKeys;
const getMoreAction = (status: ReviewStatus) => {
  if (status === 'UNDERWAY') {
    return [
      {
        label: t('caseManagement.caseReview.archive'),
        key: 'archive',
      },
      {
        type: 'divider',
        key: 'd1'
      },
      {
        label: t('common.delete'),
        key: 'delete',
        danger: true,
        permission: ['CASE_REVIEW:READ+DELETE'],
      },
    ];
  }
  return [
    {
      label: t('common.delete'),
      key: 'delete',
      danger: true,
      permission: ['CASE_REVIEW:READ+DELETE'],
    },
  ];
}
const columns: DataTableColumns<ReviewItem> = [
  {
    type: 'selection'
  },
  {
    title: "ID",
    key: 'num',
    width: 100,
    render: (record) => {
      return h(NButton, {
        text: true,
        class: 'px-0 !text-[14px] !leading-[22px]',
        onClick: () => handleOpenDetail(record.id)
      }, {default: () => h('div', {class: 'max-w-[168px]'}, {default: () => record.num})})
    }
  },
  {
    title: t('caseManagement.caseReview.name'),
    key: 'name',
    width: 200,
    // ellipsis: {tooltip: true}
  },
  {
    title: t('caseManagement.caseReview.caseCount'),
    key: 'caseCount',
    width: 100
  },
  {
    title: t('caseManagement.caseReview.status'),
    key: 'status',
    width: 150,
    render: (record) => {
      return h(StatusTag, {status: record.status})
    }
  },
  {
    title: () => {
      return h("div", {class: 'flex items-center'}, {
        default: () => {
          return [
            h('div', {}, {default: () => t('caseManagement.caseReview.passRate')}),
            h(NTooltip, {placement: 'right'}, {
              default: () => t('caseManagement.caseReview.passRateTip'),
              trigger: () => h('div', {class: 'i-carbon-ai-status ml-[4px]'})
            })
          ]
        }
      })
    },
    key: 'passRate',
    width: 200,
    render: (record) => {
      return [
        h('div', {class: 'mr-[8px] w-[100px]'}, {
          default: () => {
            return h(PassRateLine, {reviewDetail: record, height: '5px'}, {})
          }
        }),
        h('div', {}, {default: () => `${record.passRate}%`})
      ]
    }
  },
  {
    title: t('caseManagement.caseReview.type'),
    key: 'reviewPassRule',
    width: 100,
    render: (record) => {
      return h(NTag, {type: 'success', bordered: false, size: 'small'}, {
        default: () => record.reviewPassRule === 'SINGLE'
            ? t('caseManagement.caseReview.single')
            : t('caseManagement.caseReview.multi')
      })
    }
  },
  {
    title: t('caseManagement.caseReview.reviewer'),
    key: 'reviewers',
    width: 120
  },
  {
    title: t('caseManagement.caseReview.module'),
    key: 'moduleName',
    width: 120
  },
  {
    title: t('caseManagement.caseReview.tag'),
    key: 'tags',
    width: 170,
    render: (record) => {
      return h(TagGroup, {tagList: record.tags, type: 'info'})
    }
  },
  {
    title: t('common.desc'),
    key: 'description',
    width: 150,
    // ellipsis: {tooltip: true}
  },
  {
    title: t('caseManagement.caseReview.cycle'),
    key: 'cycle',
    width: 350
  },
  {
    title: t('common.operation'),
    key: 'operation',
    width: 120, fixed: 'right',
    render: (record) => {
      return h("div", {class: 'flex item-center'}, {
        default: () => [
          withDirectives(h(OButton, {
            text: true,
            content: t('common.edit'),
            class: '!mr-0',
            onClick: () => handleEditReview(record)
          }, {}), [[permission, ['CASE_REVIEW:READ+UPDATE']]]),
          h(NDivider, {vertical: true}),
          withDirectives(h(TableMoreAction, {
            list: getMoreAction(record.status),
            onSelect: ($event) => handleMoreActionSelect($event, record)
          }, {}), [[permission, ['CASE_REVIEW:READ+DELETE']]]),
        ]
      })
    }
  },
]
const handleOpenDetail = (id: string) => {
  window.$message.info(`详情功能暂未实现${id}`,)
}
const handleEditReview = (record: ReviewItem) => {
  window.$message.info(`编辑功能暂未实现${record.id}`,)
}
const handleMoreActionSelect = (item: DropdownOption, record: ReviewItem) => {
  switch (item.key) {
    case "archive":
      break;
    case "delete":
      activeRecord.value = record;
      confirmReviewName.value = '';
      break;
    default:
      break;
  }
}
const {send: fetchReviewList, data} = usePagination(() => getReviewList(reqParam.value), {
  immediate: false,
  initialData: {
    total: 0,
    data: []
  },
  data: response => response.records,
  total: response => response.total
});
const searchReview = () => {
  let moduleIds: string[] = [];
  if (props.activeFolder && props.activeFolder !== 'all') {
    moduleIds = [props.activeFolder, ...props.offspringIds];
  }
  reqParam.value = {
    ...reqParam.value,
    // keyword: keyword.value,
    // projectId: appStore.currentProjectId,
    moduleIds,
    createByMe: innerShowType.value === 'createByMe' ? userStore.id : undefined,
    reviewByMe: innerShowType.value === 'reviewByMe' ? userStore.id : undefined,
  };
  fetchReviewList();
  emit('init', reqParam.value)
}
watch(
    () => innerShowType.value,
    (value, oldValue) => {
      console.log('innerShowType', value, oldValue)
      // resetFilterParams();
      searchReview();
    }
);
watch(
    () => props.activeFolder,
    () => {
      searchReview();
    }
);
onBeforeMount(() => {
  searchReview()
})
defineExpose({
  searchReview,
});
</script>

<template>
  <div class="p-[16px]">
    <div class="mb-[16px]">
      <advance-filter :filter-config-list="filterConfigList"
                      :keyword="reqParam.keyword"
                      :search-placeholder="t('caseManagement.caseReview.list.searchPlaceholder')">
        <template #left>
          <n-radio-group size="small" v-model:value="innerShowType">
            <n-radio-button value="all">
              {{ t('common.all') }}
            </n-radio-button>
            <n-radio-button value="reviewByMe">
              {{ t('caseManagement.caseReview.waitMyReview') }}
            </n-radio-button>
            <n-radio-button value="createByMe">
              {{ t('caseManagement.caseReview.myCreate') }}
            </n-radio-button>
          </n-radio-group>
        </template>
      </advance-filter>
    </div>
    <n-data-table :bordered="false" :columns="columns" :data="data" :row-key="(tmp:ReviewItem)=>tmp.id"
                  @update:checked-row-keys="handleCheck">
      <template v-if="reqParam.keyword?.trim() === ''" #empty>
        <div class="flex w-full items-center justify-center p-[8px] text-[var(--color-text-4)]">
          {{
            hasAllPermission(['CASE_REVIEW:READ+ADD'])
                ? t('caseManagement.caseReview.tableNoData')
                : t('caseManagement.caseReview.tableNoDataNoPermission')
          }}
          <n-button v-permission="['CASE_REVIEW:READ+ADD']" class="ml-[8px]" @click="() => emit('goCreate')">
            {{ t('caseManagement.caseReview.create') }}
          </n-button>
        </div>
      </template>
    </n-data-table>
  </div>
</template>

<style scoped>

</style>