<script setup lang="ts">
import {CaseManagementTable, CaseModuleQueryParams} from "/@/models/case-management/feature-case.ts";
import {CacheTabTypeEnum} from "/@/enums/cache-tab-enum.ts";
import OCacheWrapper from "/@/components/o-cache-wrapper/index.vue";
import AdvanceFilter from '/@/components/o-advance-filter/index.vue'
import {computed, ref} from "vue";
import {FilterFormItem} from "/@/components/o-advance-filter/type.ts";
import {ViewTypeEnum} from "/@/enums/advanced-filter-enum.ts";
import {useI18n} from "vue-i18n";
import {findNodeByKey, mapTree} from "/@/utils";
import useFeatureCaseStore from "/@/store/modules/case/feature-case.ts";
import {ModuleTreeNode} from "/@/models/common.ts";
import {DataTableColumns} from "naive-ui";
import {hasAnyPermission} from "/@/utils/permission.ts";
import {CaseManagementRouteEnum} from "/@/enums/route-enum.ts";
import {useAppStore} from "/@/store";
import {useRoute, useRouter} from "vue-router";

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
const {t} = useI18n()
const router = useRouter();
const route = useRoute();
const appStore = useAppStore();
const featureCaseStore = useFeatureCaseStore()
const showType = ref<string>('list');
const advanceFilterRef = ref<InstanceType<typeof AdvanceFilter> | null>(null);
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
const columns: DataTableColumns<CaseManagementTable> = [
  {
    type: 'selection'
  },
  {
    title: "ID",
    key: 'num',
    width: 150,
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
    width: 150
  },
  {
    title: t('caseManagement.featureCase.tableColumnExecutionResult'),
    key: 'lastExecuteResult',
    width: 150
  },
  {
    title: t('caseManagement.featureCase.tableColumnModule'),
    key: 'moduleId',
    width: 200
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
  // customFieldsColumns
  {
    title: hasOperationPermission.value ? t('caseManagement.featureCase.tableColumnActions') : '',
    key: 'operation',
    width: hasOperationPermission.value ? 140 : 50,
  },
]
const handleCaseDetail = () => {
  router.push({
    name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
  });
}
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
                        :name="moduleNamePath">
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
          <n-data-table :columns="columns"/>
        </div>
      </o-cache-wrapper>
    </keep-alive>

  </div>
</template>

<style scoped>

</style>