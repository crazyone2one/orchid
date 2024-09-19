<script setup lang="ts">
import {CaseManagementTable, CaseModuleQueryParams, CustomAttributes} from "/@/models/case-management/feature-case.ts";
import {CacheTabTypeEnum} from "/@/enums/cache-tab-enum.ts";
import OCacheWrapper from "/@/components/o-cache-wrapper/index.vue";
import AdvanceFilter from '/@/components/o-advance-filter/index.vue'
import {computed, h, onMounted, ref, resolveDirective, watch, withDirectives} from "vue";
import {FilterFormItem, FilterResult} from "/@/components/o-advance-filter/type.ts";
import {ViewTypeEnum} from "/@/enums/advanced-filter-enum.ts";
import {useI18n} from "vue-i18n";
import {findNodeByKey, findNodePathByKey, mapTree} from "/@/utils";
import useFeatureCaseStore from "/@/store/modules/case/feature-case.ts";
import {BatchActionQueryParams, ModuleTreeNode, TableQueryParams} from "/@/models/common.ts";
import {type DataTableColumns, NDivider, NTooltip, NTreeSelect} from "naive-ui";
import {hasAnyPermission} from "/@/utils/permission.ts";
import {CaseManagementRouteEnum} from "/@/enums/route-enum.ts";
import {useAppStore} from "/@/store";
import {useRoute, useRouter} from "vue-router";
import {usePagination} from "alova/client";
import {
  getCaseDefaultFields,
  getCaseDetail,
  getCaseList,
  updateCaseRequest
} from "/@/api/modules/case-management/feature-case.ts";
import {cloneDeep} from "lodash-es";
import Pagination from "/@/components/o-pagination/index.vue";
import {statusIconMap} from "/@/views/case-management/case-management-feature/components/utils.ts";
import ExecuteResult from "/@/components/o-case-associate/ExecuteResult.vue";
import OButton from "/@/components/o-button/index.vue";

const props = defineProps<{
  activeFolder: string;
  moduleName: string;
  offspringIds: string[]; // 当前选中文件夹的所有子孙节点id
  modulesCount: Record<string, number>; // 模块数量
}>();

const emit = defineEmits<{
  (e: 'init', params: CaseModuleQueryParams, refreshModule?: boolean): void;
  (e: 'initModules'): void;
  (e: 'setActiveFolder'): void;
}>();
const permission = resolveDirective('permission')
const {t} = useI18n()
const router = useRouter();
const route = useRoute();
const appStore = useAppStore();
const featureCaseStore = useFeatureCaseStore()
const showType = ref<string>('list');
const advanceFilterRef = ref<InstanceType<typeof AdvanceFilter> | null>(null);
const isAdvancedSearchMode = computed(() => advanceFilterRef.value?.isAdvancedSearchMode);
const currentProjectId = computed(() => appStore.currentProjectId);
const filterConfigList = computed<FilterFormItem[]>(() => {
  return []
})
const caseTreeData = computed(() => {
  return mapTree<ModuleTreeNode>(featureCaseStore.caseTree, (e) => {
    return {
      ...e,
      draggable: false,
    };
  });
});
const moduleNamePath = computed(() => {
  return props.activeFolder === 'all'
      ? t('caseManagement.featureCase.allCase')
      : findNodeByKey<Record<string, any>>(caseTreeData.value, props.activeFolder, 'id')?.name;
});
const hasOperationPermission = computed(() =>
    hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE', 'FUNCTIONAL_CASE:READ+DELETE'])
);
// 处理自定义字段列
let customFieldsColumns: DataTableColumns<CaseManagementTable> = [];
const initDefaultFields = ref<CustomAttributes[]>([]);
const caseLevelFields = ref<Record<string, any>>({});

const caseLevelList = computed(() => {
  return caseLevelFields.value?.options || [];
});

const getDefaultFields = async () => {
  customFieldsColumns = [];
  const result = await getCaseDefaultFields(currentProjectId.value);
  initDefaultFields.value = result.customFields;
  customFieldsColumns = initDefaultFields.value
      .filter((item: any) => !item.internal)
      .map((item: any) => {
        return {
          title: item.fieldName,
          slotName: item.fieldId as string,
          key: item.fieldId,
          showInTable: false,
          showDrag: true,
          width: 300,
        };
      });
  caseLevelFields.value = result.customFields.find(
      (item: any) => item.internal && item.internalFieldKey === 'functional_priority'
  );
}
const columns: DataTableColumns<CaseManagementTable> = [
  {
    type: 'selection'
  },
  {
    title: "ID",
    key: 'num',
    width: 80,
    fixed: 'left'
  },
  {
    title: t('caseManagement.featureCase.tableColumnName'),
    key: 'name',
    width: 180
  },
  {
    title: t('caseManagement.featureCase.tableColumnLevel'),
    key: 'caseLevel',
    width: 150
  },
  {
    title: t('caseManagement.featureCase.tableColumnReviewResult'),
    key: 'reviewStatus',
    width: 150,
    render: (record) => {
      return h("div", {class: 'flex items-center'}, {
        default: () => [
          h('div', {class: statusIconMap[record.reviewStatus as string].icon}, {}),
          h('span', {}, {default: () => statusIconMap[record.reviewStatus as string].statusText || ''})
        ]
      })
    }
  },
  {
    title: t('caseManagement.featureCase.tableColumnExecutionResult'),
    key: 'lastExecuteResult',
    width: 150,
    render: (record) => {
      return h(ExecuteResult, {executeResult: record.lastExecuteResult}, {})
    }
  },
  {
    title: t('caseManagement.featureCase.tableColumnModule'),
    key: 'moduleId',
    width: 150,
    render: (record) => {
      if (record.showModuleTree) {
        return h(NTreeSelect, {
          value: record.moduleId, options: caseTreeData.value,
          labelField: 'name', keyField: 'id', filterable: true,
          class: 'param-input w-full',
          onUpdateValue: (value) => handleChangeModule(value, record)
        }, {})
      }
      return h(NTooltip, {}, {
        default: () => getModules(record.moduleId),
        trigger: () => h('span', {
              onClick: () => record.showModuleTree = true,
              class: 'one-line-text inline-block'
            },
            {default: () => getModules(record.moduleId)})
      });
    }
  },
  {
    title: t('caseManagement.featureCase.tableColumnTag'),
    key: 'tags',
    width: 300
  },
  {
    title: t('caseManagement.featureCase.tableColumnUpdateUser'),
    key: 'updateUserName',
    width: 200
  },
  {
    title: t('caseManagement.featureCase.tableColumnUpdateTime'),
    key: 'updateTime',
    width: 200
  },
  {
    title: t('caseManagement.featureCase.tableColumnCreateUser'),
    key: 'createUserName',
    width: 200
  },
  {
    title: t('caseManagement.featureCase.tableColumnCreateTime'),
    key: 'createTime',
    width: 200
  },
  ...customFieldsColumns,
  {
    title: hasOperationPermission.value ? t('caseManagement.featureCase.tableColumnActions') : '',
    key: 'operation',
    width: hasOperationPermission.value ? 180 : 50,
    fixed: 'right',
    render: (record) => {
      return [
        withDirectives(h(OButton, {
          text: true,
          content: t('common.edit'),
          class: '!mr-0'
        }, {}), [[permission, ['FUNCTIONAL_CASE:READ+UPDATE']]]),
        withDirectives(h(NDivider, {
          vertical: true,
          class: '!mx-2 h-[12px]'
        }, {}), [[permission, ['FUNCTIONAL_CASE:READ+UPDATE']]]),
        withDirectives(h(OButton, {
          text: true,
          content: t('caseManagement.featureCase.copy'),
          class: '!mr-0'
        }, {}), [[permission, ['FUNCTIONAL_CASE:READ+ADD']]]),
        withDirectives(h(NDivider, {
          vertical: true,
          class: '!mx-2 h-[12px]'
        }, {}), [[permission, ['FUNCTIONAL_CASE:READ+ADD']]]),
        withDirectives(h(OButton, {
          text: true, type: 'error',
          content: t('common.delete'),
          class: '!mr-0',
          onClick: () => deleteCase(record)
        }, {}), [[permission, ['FUNCTIONAL_CASE:READ+DELETE']]])
      ]
    }
  },
]

const deleteCase = (record: CaseManagementTable) => {
  console.log(record)
}
const handleChangeModule = async (value: string, option: CaseManagementTable) => {
  const detailResult = await getCaseDetail(option.id);
  const params = {
    request: {
      ...detailResult,
      moduleId: value,
      customFields: getCustomMaps(detailResult),
    },
    fileList: [],
  };
  await updateCaseRequest(params);
  window.$message.success(t('common.updateSuccess'));
  option.showModuleTree = false;
  initData();
}
const getCustomMaps = (detailResult: CaseManagementTable) => {
  const {customFields} = detailResult;
  return customFields.map((item: any) => {
    return {
      fieldId: item.fieldId,
      value: Array.isArray(item.defaultValue) ? JSON.stringify(item.defaultValue) : item.defaultValue,
    };
  });
}
const getModules = (moduleIds: string) => {
  const modules = findNodePathByKey(caseTreeData.value, moduleIds, undefined, 'id');
  if (modules) {
    const moduleName = (modules || [])?.treePath.map((item: any) => item.name);
    if (moduleName.length === 1) {
      return moduleName[0];
    }
    return `/${moduleName.join('/')}`;
  }
}
const handleCaseDetail = () => {
  router.push({
    name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
  });
}
const reqParam = ref<TableQueryParams>({
  current: 1,
  pageSize: 10,
  keyword: ''
})
const {send: fetchCaseData, data, page, pageSize, total} = usePagination((page, pageSize) => {
  reqParam.value.current = page
  reqParam.value.pageSize = pageSize
  return getCaseList(reqParam.value)
}, {
  immediate: false,// 请求前的初始数据（接口返回的数据格式）
  initialData: {
    total: 0,
    data: []
  },
  data: response => response.records,
  total: response => response.totalRow
})
const handleSetPage = (param: number) => page.value = param
const handleSetPageSize = (param: number) => pageSize.value = param
const initBatchParams: BatchActionQueryParams = {
  selectedIds: [],
  selectAll: false,
  excludeIds: [],
  currentSelectCount: 0,
};
const batchParams = ref<BatchActionQueryParams>(cloneDeep(initBatchParams));
const initTableParams = () => {
  let moduleIds: string[] = []
  if (props.activeFolder !== 'all' && !isAdvancedSearchMode.value) {
    // moduleIds = [props.activeFolder];
    moduleIds = [props.activeFolder, ...props.offspringIds];
  }
  return {
    moduleIds,
    projectId: currentProjectId.value,
    excludeIds: batchParams.value.excludeIds || [],
    selectAll: batchParams.value.selectAll,
    selectIds: batchParams.value.selectedIds || [],
  };
};
const emitTableParams = (refreshModule = false) => {
  if (isAdvancedSearchMode.value) return;
  const tableParams: CaseModuleQueryParams = initTableParams();
  emit(
      'init',
      {
        moduleIds: tableParams.moduleIds,
        ...reqParam.value,
        projectId: currentProjectId.value
      },
      refreshModule
  );
}
const initData = () => {
  reqParam.value = {...reqParam.value, ...initTableParams()}
  fetchCaseData()
  emitTableParams()
}
const fetchData = (key = '') => {
  reqParam.value.keyword = key
  initData();
}
const handleAdvSearch = (filter: FilterResult, id: string) => {
  emit('setActiveFolder');
}
watch(() => props.activeFolder, (value) => {

  if (props.activeFolder !== 'recycle' && value && !isAdvancedSearchMode.value) {
    initData()
  }
}, {deep: true})
watch(() => showType.value, (val) => {
      if (val === 'list') {
        initData();
      }
    }
);
onMounted(() => {
  initData()
  emitTableParams()
})
getDefaultFields();
</script>

<template>
  <div class="h-full">
    <keep-alive :include="[CacheTabTypeEnum.CASE_MANAGEMENT_TABLE_FILTER]">
      <o-cache-wrapper v-if="showType === 'list'"
                       :key="CacheTabTypeEnum.CASE_MANAGEMENT_TABLE_FILTER"
                       :cache-name="CacheTabTypeEnum.CASE_MANAGEMENT_TABLE_FILTER">
        <advance-filter ref="advanceFilterRef" :filter-config-list="filterConfigList"
                        :view-type="ViewTypeEnum.FUNCTIONAL_CASE"
                        :search-placeholder="t('caseManagement.featureCase.searchPlaceholder')"
                        :count="props.modulesCount[props.activeFolder] || 0"
                        :name="moduleNamePath"
                        @adv-search="handleAdvSearch"
                        @refresh="fetchData()">
          <template #left>
            <div>
              <n-button v-permission="['FUNCTIONAL_CASE:READ+ADD']" type="primary" class="mr-[12px]"
                        @click="handleCaseDetail">
                {{ t('common.newCreate') }}
              </n-button>
            </div>
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
        <div class="mt-2">
          <n-data-table :columns="columns" :data="data" :row-key="(tmp:CaseManagementTable)=>tmp.id"/>
          <div class="mt-8">
            <pagination :page-size="pageSize" :page="page" :count="total"
                        @update-page="handleSetPage"
                        @update-page-size="handleSetPageSize"/>
          </div>
        </div>
      </o-cache-wrapper>
    </keep-alive>

  </div>
</template>

<style scoped>

</style>