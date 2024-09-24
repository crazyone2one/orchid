<script setup lang="ts">
import ODrawer from '/@/components/o-drawer/index.vue';
import {ModuleTreeNode, TableQueryParams} from "/@/models/common.ts";
import {CaseManagementTable} from "/@/models/case-management/feature-case.ts";
import {CaseLinkEnum} from "/@/enums/case-enum.ts";
import {useVModel} from "@vueuse/core";
import {computed, h, ref, watch, watchEffect} from "vue";
import {initGetModuleCountFunc, RequestModuleEnum} from "/@/components/o-case-associate/utils.ts";
import TreeFolderAll from "/@/views/case-management/case-review/components/TreeFolderAll.vue";
import {ProtocolKeyEnum} from "/@/enums/api-enum.ts";
import {useI18n} from "vue-i18n";
import OTree from '/@/components/o-tree/index.vue'
import AdvanceFilter from '/@/components/o-advance-filter/index.vue'
import {FilterFormItem} from "/@/components/o-advance-filter/type.ts";
import {DataTableColumns, DataTableRowKey, SelectOption} from "naive-ui";
import {usePagination, useRequest} from "alova/client";
import {mapTree} from "/@/utils";
import TagGroup from "/@/components/o-tag-group/index.vue";
import {TreeNodeData} from "/@/components/o-tree/types.ts";

const props = withDefaults(
    defineProps<{
      visible: boolean;
      projectId?: string; // 项目id
      caseId?: string; // 用例id  用例评审那边不需要传递
      getModulesFunc: (params: TableQueryParams) => any; // 获取模块树请求
      modulesParams?: Record<string, any>; // 获取模块树请求
      getTableFunc: (params: TableQueryParams) => any; // 获取表请求函数
      tableParams?: TableQueryParams; // 查询表格的额外的参数
      currentSelectCase: CaseLinkEnum; // 当前选中的用例类型
      moduleOptions?: { label: string; value: string }[]; // 功能模块对应用例下拉
      confirmLoading: boolean;
      associatedIds: string[]; // 已关联用例id集合用于去重已关联
      hasNotAssociatedIds?: string[];
      type: RequestModuleEnum[keyof RequestModuleEnum];
      moduleCountParams?: TableQueryParams; // 获取模块树数量额外的参数
      hideProjectSelect?: boolean; // 是否隐藏项目选择
      isHiddenCaseLevel?: boolean;
      selectorAll?: boolean;
      reviewersOptions?: SelectOption[]
    }>(),
    {
      isHiddenCaseLevel: false,
      selectorAll: false,
    }
);
const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void;
  (e: 'update:projectId', val: string): void;
  (e: 'init', val: TableQueryParams): void; // 初始化模块数量
  (e: 'close'): void;
  (e: 'save', params: any): void; // 保存对外传递关联table 相关参数
}>();
const {t} = useI18n()
const innerVisible = useVModel(props, 'visible', emit);
const innerProject = ref<string | undefined>(props.projectId);
const caseType = ref<CaseLinkEnum>(props.currentSelectCase);
const moduleKeyword = ref('');
const folderTree = ref<ModuleTreeNode[]>([]);
const modulesCount = ref<Record<string, any>>({});
const isExpandAll = ref(false);
const treeFolderAllRef = ref<InstanceType<typeof TreeFolderAll>>();
const activeFolder = ref('all');
const activeFolderName = ref(t('ms.case.associate.allCase'));
const selectedModuleKeys = ref<string[]>([]);
const filterConfigList = ref<FilterFormItem[]>([]);
const version = ref('');
const keyword = ref('');
const combine = ref<Record<string, any>>({});
const offspringIds = ref<string[]>([]);
const selectedProtocols = computed<string[]>(() => treeFolderAllRef.value?.selectedProtocols ?? []);
const searchParams = ref<TableQueryParams>({
  moduleIds: [],
  version: version.value,
  current: 1,
  pageSize: 10,
  projectId: innerProject.value,
  excludeIds: [...props.associatedIds], // 已经存在的关联的id列表
  condition: {
    keyword: keyword.value,
  },
  protocols: caseType.value === 'API' ? selectedProtocols.value : [],
});
const getLoadListParams = () => {
  if (activeFolder.value === 'all' || !activeFolder.value) {
    searchParams.value.moduleIds = [];
  } else {
    searchParams.value.moduleIds = [activeFolder.value, ...offspringIds.value];
  }
  if (props.moduleOptions && props.moduleOptions.length) {
    searchParams.value.sourceType = caseType.value;
    searchParams.value.sourceId = props.caseId;
  }
};

const initModuleCount = async () => {
  const params = {
    keyword: keyword.value,
    moduleIds: selectedModuleKeys.value,
    projectId: innerProject.value,
    current: searchParams.value.current,
    pageSize: searchParams.value.pageSize,
    combine: combine.value,
    sourceId: props.caseId,
    sourceType: caseType.value,
    ...props.tableParams,
    ...props.moduleCountParams,
    // filter: propsRes.value.filter,
    protocols: caseType.value === 'API' ? selectedProtocols.value : [],
  };
  modulesCount.value = await initGetModuleCountFunc(props.type, params);
}
const {send: fetchCaseList, data, total} = usePagination(() => props.getTableFunc(searchParams.value), {
  immediate: false,
  initialData: {
    total: 0,
    data: []
  },
  data: response => response.records,
  total: response => response.total
});
const searchCase = () => {
  getLoadListParams();
  fetchCaseList()
  initModuleCount()
}
const setActiveFolder = (id: string) => {
  activeFolder.value = id;
  activeFolderName.value = t('ms.case.associate.allCase');
  selectedModuleKeys.value = [];
}
const checkedRowKeys = ref<DataTableRowKey[]>([]);
const handleCheck = (rowKeys: DataTableRowKey[]) => checkedRowKeys.value = rowKeys;
const selectedProtocolsChange = () => {
  initModules();
  // setAllSelectModule();
  // initFilter();
}
const getCaseLevelColumn = () => {
  if (!props.isHiddenCaseLevel) {
    return [
      {
        title: t('ms.case.associate.caseLevel'),
        key: 'caseLevel',
        width: 150,
      }
    ]
  }
  return []
}
const getReviewStatus = () => {
  if (!props.isHiddenCaseLevel) {
    return [
      {
        title: t('caseManagement.featureCase.tableColumnReviewResult'),
        key: 'reviewStatus',
        width: 150,
      },
      {
        title: t('caseManagement.featureCase.tableColumnExecutionResult'),
        key: 'lastExecuteResult',
        width: 150,
      }
    ]
  }
  return [];
}
const columns: DataTableColumns<CaseManagementTable> = [
  {
    type: 'selection'
  },
  {
    title: "ID",
    key: 'num',
    width: 100,
    fixed: 'left',
  },
  {
    title: t('ms.case.associate.caseName'),
    key: 'name',
    width: 250,
  },
  ...getCaseLevelColumn(),
  ...getReviewStatus(),
  {
    title: t('ms.case.associate.tags'),
    key: 'tags',
    render: (record) => {
      return h(TagGroup, {tagList: record.tags, type: 'info'})
    }
  },
  {
    title: t('caseManagement.featureCase.tableColumnCreateUser'),
    key: 'createUserName',
    width: 200,
  },
]
const {send: fetchModules, loading} = useRequest((params) => props.getModulesFunc(params), {
  immediate: false,
  force: true
})
const initModules = (isSetDefaultKey = false) => {
  let params = {
    projectId: innerProject.value,
    sourceType: props.moduleOptions && props.moduleOptions.length ? caseType.value : undefined,
    sourceId: props.moduleOptions && props.moduleOptions.length ? props.caseId : undefined,
  };
  if (props.modulesParams) {
    params = {
      ...params,
      ...props.modulesParams,
    };
  }
  fetchModules(params).then(res => {
    folderTree.value = mapTree<ModuleTreeNode>(res, (e) => {
      return {
        ...e,
        hideMoreAction: e.id === 'root',
        draggable: false,
        disabled: false,
        count: modulesCount.value[e.id] || 0,
      };
    });
    if (isSetDefaultKey) {
      selectedModuleKeys.value = [folderTree.value[0].id];
      [activeFolder.value] = [folderTree.value[0].id];
      activeFolderName.value = folderTree.value[0].name;
      const offspringIds: string[] = [];
      mapTree(folderTree.value[0].children || [], (e) => {
        offspringIds.push(e.id);
        return e;
      });
    }
  })

};
const initProjectList = (setDefault: boolean) => {
  initModules();
}
const changeCaseType = (value: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[]) => {
  caseType.value = value as CaseLinkEnum;
  initModules();
  // setAllSelectModule();
  // initFilter();
}
const cancel = () => {
  innerVisible.value = false;
  keyword.value = '';
  version.value = '';
  searchParams.value = {
    moduleIds: [],
    version: '',
    current: 1,
    pageSize: 10,
    projectId: innerProject.value,
  };
  activeFolder.value = 'all';
  activeFolderName.value = t('ms.case.associate.allCase');
  checkedRowKeys.value = [];
  emit('close');
}
const folderNodeSelect = (_selectedKeys: string[], node: TreeNodeData) => {
  selectedModuleKeys.value = _selectedKeys as string[];
  activeFolder.value = node.id;
  activeFolderName.value = node.name;
  offspringIds.value = [];
  mapTree(node.children || [], (e) => {
    offspringIds.value.push(e.id);
    return e;
  });
}
const handleConfirm = () => {
  const {versionId, moduleIds} = searchParams.value;
  const params = {
    // excludeIds: [...excludeKeys],
    // selectIds: selectorStatus === 'all' ? [] : [...checkedRowKeys.value],
    selectIds: [...checkedRowKeys.value],
    // selectAll: selectorStatus === 'all',
    moduleIds,
    versionId,
    refId: '',
    sourceType: caseType.value,
    projectId: innerProject.value,
    sourceId: props.caseId,
    totalCount: total.value,
    condition: {
      keyword: keyword.value,
    },
  };

  emit('save', params)
}
watch(
    () => props.visible,
    (val) => {
      if (val) {
        initModules()
        // resetFilterParams();
        // resetSelector();
        if (!props.hideProjectSelect) {
          initProjectList(true);
        }
      } else {
        cancel();
      }
    }
);
watch(
    () => activeFolder.value,
    () => {
      searchCase();
    }
);
watchEffect(() => {
  searchCase();
})
defineExpose({
  initModules,
});
</script>

<template>
  <o-drawer v-model:visible="innerVisible" :width="1200" :footer="false" :title="$t('ms.case.associate.title')"
            @cancel="cancel">
    <template #headerLeft>
      <div class="float-left">
        <n-select v-if="props.moduleOptions" v-model:value="caseType" class="ml-2 max-w-[100px]"
                  :placeholder="$t('caseManagement.featureCase.PleaseSelect')"/>
      </div>
    </template>
    <div class="flex h-full">
      <div class="w-[292px] border-r border-[var(--color-text-n8)] p-[16px]">
        <div class="flex items-center justify-between">
          <div v-if="!props.hideProjectSelect" class="w-full max-w-[259px]">
            <n-select v-model:value="innerProject" class="mb-[16px] w-full" :default-value="innerProject" filterable
                      :placeholder="$t('common.pleaseSelect')"/>
          </div>
        </div>
        <div class="mb-[8px] flex items-center gap-[8px]">
          <n-input v-model:value="moduleKeyword"
                   :placeholder="$t('caseManagement.caseReview.folderSearchPlaceholder')" clearable :maxlength="255"/>
          <n-tooltip>
            <template #trigger>
              <n-button @click="isExpandAll = !isExpandAll" type="primary" text>
                <template #icon>
                  <div
                      :class="isExpandAll?'i-carbon-ibm-watson-tone-analyzer':'i-carbon-ibm-watsonx-assistant'"/>
                </template>
              </n-button>
            </template>
            {{ isExpandAll ? $t('common.collapseAllSubModule') : $t('common.expandAllSubModule') }}
          </n-tooltip>
        </div>
        <tree-folder-all ref="treeFolderAllRef"
                         :protocol-key="ProtocolKeyEnum.CASE_MANAGEMENT_ASSOCIATE_PROTOCOL"
                         :active-folder="activeFolder"
                         :folder-name="t('caseManagement.featureCase.allCase')"
                         :all-count="modulesCount.total || modulesCount.all || 0"
                         :show-expand-api="false"
                         :not-show-operation="caseType !== 'API'"
                         @set-active-folder="setActiveFolder"
                         @selected-protocols-change="selectedProtocolsChange"/>
        <n-spin :show="loading">
          <o-tree :data="folderTree" :keyword="moduleKeyword" @select="folderNodeSelect"/>
        </n-spin>
      </div>
      <div class="flex w-[calc(100%-293px)] flex-col p-[16px]">
        <advance-filter :filter-config-list="filterConfigList"
                        :search-placeholder="t('caseManagement.featureCase.searchByNameAndId')"
                        @refresh="searchCase">
          <template #left>
            <div class="flex items-center justify-between">
              <div class="flex items-center">
                <div class="one-line-text mr-[4px] max-w-[300px] text-[var(--color-text-1)]">
                  {{ activeFolderName }}
                </div>
                <!--                <div class="text-[var(&#45;&#45;color-text-4)]">({{ propsRes.msPagination?.total }})</div>-->
              </div>
            </div>
          </template>
        </advance-filter>
        <n-data-table :columns="columns" :data="data" :row-key="(tmp:CaseManagementTable)=>tmp.id"
                      @update:checked-row-keys="handleCheck"/>
        <div class="footer">
          <div class="flex flex-1 items-center">
            <slot name="footerLeft"></slot>
          </div>
          <div class="flex items-center">
            <slot name="footerRight">
              <n-button secondary :disabled="props.confirmLoading" class="mr-[12px]" @click="cancel">
                {{ t('common.cancel') }}
              </n-button>
              <n-button
                  type="primary"
                  :loading="props.confirmLoading"
                  @click="handleConfirm"
              >
                {{ t('ms.case.associate.associate') }}
              </n-button>
            </slot>
          </div>
        </div>
      </div>
    </div>
  </o-drawer>
</template>

<style scoped>
.footer {
  @apply flex items-center justify-between;

  margin: auto -16px -16px;
  padding: 12px 16px;
  box-shadow: 0 -1px 4px 0 rgb(31 35 41 / 10%);
}
</style>