<script setup lang="ts">
import DetailDrawer from '/@/components/o-detail-drawer/index.vue';
import {computed, defineAsyncComponent, ref, watch} from "vue";
import useFeatureCaseStore from "/@/store/modules/case/feature-case.ts";
import {useI18n} from "vue-i18n";
import {getCaseDetail, getCaseModuleTree} from "/@/api/modules/case-management/feature-case.ts";
import {cloneDeep} from "lodash-es";
import {CustomAttributes, DetailCase, TabItemType} from "/@/models/case-management/feature-case.ts";
import {defaultCaseDetail} from "/@/config/case-management.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";
import OCaseLevel from "/@/components/o-case-associate/CaseLevel.vue";
import type {CaseLevel} from '/@/components/o-case-associate/types.ts'
import type {PaginationInfo} from "/@/components/o-pagination/types.ts";
import {
  buggerTab,
  caseTab,
  getCaseLevels,
  tabDefaultSettingList
} from "/@/views/case-management/case-management-feature/components/utils.ts";
import {ModuleTreeNode} from "/@/models/common.ts";
import {useAppStore} from "/@/store";
import {useRequest} from "alova/client";
import OTab from '/@/components/o-tab/index.vue'
import InputCommon from '/@/components/o-common/index.vue'
import {FormRuleItem} from "/@/views/case-management/case-management-feature/components/types.ts";

const props = defineProps<{
  visible: boolean;
  detailId: string; // 详情 id
  detailIndex: number; // 详情 下标
  tableData: any[]; // 表格数据
  pagination: PaginationInfo; // 分页器对象
  pageChange: (page: number) => void; // 分页变更函数
  isEdit?: boolean; // 编辑状态
}>();

const emit = defineEmits(['update:visible', 'success']);
const {t} = useI18n()
const featureCaseStore = useFeatureCaseStore();
const appStore = useAppStore();
const detailDrawerRef = ref<InstanceType<typeof DetailDrawer>>();
const currentProjectId = computed(() => appStore.currentProjectId);
const showDrawerVisible = ref<boolean>(false);
const showSettingDrawer = ref<boolean>(false);
const activeTab = ref<string | number>('detail');
const isEditTitle = ref<boolean>(false);
const detailInfo = ref<DetailCase>(cloneDeep(defaultCaseDetail));
const caseLevels = ref<CaseLevel>('P0');
const customFields = ref<CustomAttributes[]>([]);
const titleName = ref('');
const caseTree = ref<ModuleTreeNode[]>([]);
const commentInputRef = ref<InstanceType<typeof InputCommon>>();
const commentInputIsActive = computed(() => commentInputRef.value?.isActive);
const TabDetail = defineAsyncComponent(() => import('./tab-content/TabDetail.vue'));
const BaseInfo = defineAsyncComponent(() => import('./tab-content/BaseInfo.vue'));
const formItem = ref<FormRuleItem[]>([]);
const handleClickTitle = () => {
  if (hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE'])) {
    isEditTitle.value = true;
  }
}
const loadedCase = (detail: DetailCase) => {
  getCaseTree();
  detailInfo.value = {...detail};
  titleName.value = detail.name;
  setCount(detail);
  customFields.value = detailInfo.value?.customFields as CustomAttributes[];
  caseLevels.value = getCaseLevels(customFields.value) as CaseLevel;
}
const {send: fetchCaseTree,} = useRequest(() => getCaseModuleTree({ projectId: currentProjectId.value }), {
  immediate: false,
  force: true
})
const getCaseTree = () => fetchCaseTree().then(res => caseTree.value = res)
const setCount = (detail: DetailCase) => {
  const {
    bugCount,
    caseCount,
    caseReviewCount,
    demandCount,
    relateEdgeCount,
    testPlanCount,
    commentCount,
    historyCount,
  } = detail;
  const countMap: Record<string, any> = {
    case: caseCount || '0',
    dependency: relateEdgeCount || '0',
    caseReview: caseReviewCount || '0',
    testPlan: testPlanCount || '0',
    bug: bugCount || '0',
    requirement: demandCount || '0',
    comments: commentCount || '0',
    changeHistory: historyCount || '0',
  };
  featureCaseStore.initCountMap(countMap);
}
const newTabDefaultSettingList = computed(() => {
  if (appStore.currentMenuConfig.includes('bugManagement')) {
    return [...tabDefaultSettingList, ...buggerTab, ...caseTab];
  }
  return [...tabDefaultSettingList, ...caseTab];
});
const tabSetting = ref<TabItemType[]>([]);
const initTabConfig = async () => {
  const result = (await featureCaseStore.getContentTabList()) || [];
  if (result.length) {
    return result.filter((item) => item.isShow);
  }
  return newTabDefaultSettingList.value;
}
initTabConfig().then(res => tabSetting.value = res);
const initTab = async () => {
  showSettingDrawer.value = false;
  const tmpArr = (await featureCaseStore.getContentTabList()) || [];
  tabSetting.value = tmpArr.filter((item) => item.isShow);
};
const getTotal = (key: string) => {
  switch (key) {
    case 'detail':
      return '';
    default:
      break;
  }
  const count = featureCaseStore.countMap[key] ?? 0;
  return featureCaseStore.countMap[key] > 99 ? '99+' : `${count > 0 ? count : ''}`;
}
const clickMenu = (key: string | number) => {
  activeTab.value = key;
  featureCaseStore.setActiveTab(key);
  switch (activeTab.value) {
    case 'setting':
      showMenuSetting();
      break;
    default:
      showSettingDrawer.value = false;
      break;
  }
}
const showMenuSetting = () => {
  showSettingDrawer.value = true;
}
const updateSuccess = () => {
  detailDrawerRef.value?.initDetail();
  emit('success');
}
watch(
    () => props.visible,
    (val) => {
      if (val) {
        showDrawerVisible.value = val;
        activeTab.value = 'detail';
        featureCaseStore.setActiveTab(activeTab.value);
      } else {
        activeTab.value = '';
        isEditTitle.value = false;
      }
    }
);

watch(
    () => showDrawerVisible.value,
    (val) => {
      emit('update:visible', val);
    }
);
</script>

<template>
  <detail-drawer ref="detailDrawerRef" v-model:visible="showDrawerVisible"
                 :title="t('caseManagement.featureCase.caseDetailTitle', { id: detailInfo?.num, name: detailInfo?.name })"
                 :width="1260" :detail-id="props.detailId" :get-detail-func="getCaseDetail" :footer="false"
                 :pagination="!isEditTitle ? props.pagination : undefined" :table-data="props.tableData"
                 :page-change="props.pageChange"
                 :mask="false"
                 :mask-closable="true"
                 @loaded="loadedCase">
    <template #titleName>
      <div :class="`case-title flex items-center gap-[8px] ${isEditTitle ? 'w-full' : ''}`">
        <div v-if="!isEditTitle" class="flex items-center">
          <o-case-level :case-level="caseLevels"/>
        </div>
        <n-input v-if="isEditTitle" :class="`edit-title w-full ${titleName.length ? '' : 'edit-title-error'}`"
                 placeholder="t('case.caseNamePlaceholder')"/>
        <div v-else class="flex items-center">
          <div> [ {{ detailInfo?.num }} ]</div>
          <div
              :class="`${
              hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE']) ? 'hover-title-name' : ''
            } one-line-text max-w-[200px] cursor-pointer`"
              @click="handleClickTitle"
          >
            {{ detailInfo.name }}
          </div>
        </div>
      </div>
    </template>
    <template #titleRight="{loading}">
      <div class="rightButtons flex items-center">
        <n-button v-if="hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE']) && !isEditTitle"
                  secondary size="small" class="mr-4 !rounded-[2px]" :disabled="loading">
          <template #icon>
            <div class="mr-2 font-[16px] i-carbon-edit"/>
          </template>
          {{ t('common.edit') }}
        </n-button>
        <n-button v-if="hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE']) && !isEditTitle"
                  secondary class="mr-4 !rounded-[2px]" :disabled="loading">
          <template #icon>
            <div class="mr-2 font-[16px] i-carbon-share"/>
          </template>
          {{ t('caseManagement.featureCase.share') }}
        </n-button>
        <n-button v-if="hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE']) && !isEditTitle"
                  secondary class="mr-4 !rounded-[2px]" :disabled="loading">
          <template #icon>
            <div class="mr-2 font-[16px] i-carbon-share"/>
          </template>
          {{ t('caseManagement.featureCase.follow') }}
        </n-button>
      </div>
    </template>
    <template #default="{ detail, loading }">
      <div ref="wrapperRef" class="bg-white">
        <div class="header relative h-[48px] border-b pl-2">
          <div class="max-w-[calc(100%-100px)]">
            <o-tab
                v-model:active-key="activeTab"
                :content-tab-list="tabSetting"
                :get-text-func="getTotal"
                class="no-content relative"
                @change="clickMenu"
            />
          </div>
          <span class="display-setting h-full text-[var(--color-text-2)]" @click="showMenuSetting">
            {{ t('caseManagement.featureCase.detailDisplaySetting') }}
          </span>
        </div>
        <div>
          <div
              :class="`${
              !commentInputIsActive ? 'h-[calc(100vh-174px)]' : 'h-[calc(100vh-378px)]'
            } overflow-y-auto overflow-x-hidden w-full p-[16px] pt-4`"
          >
            <template v-if="activeTab === 'detail'">
              <tab-detail :form="detailInfo"
                          :allow-edit="true"
                          :is-edit="props.isEdit"
                          :form-rules="formItem"
                          @update-success="updateSuccess"/>
            </template>
            <template v-if="activeTab === 'basicInfo'">
              <base-info :loading="loading" :detail="detail" @update-success="updateSuccess"/>
            </template>
            <template v-if="activeTab === 'requirement'">
              requirement
            </template>
            <template v-if="activeTab === 'case'">
              case
            </template>
            <template v-if="activeTab === 'bug'">
              bug
            </template>
            <template v-if="activeTab === 'dependency'">
              dependency
            </template>
            <template v-if="activeTab === 'caseReview'">
              caseReview
            </template>
            <template v-if="activeTab === 'testPlan'">
              testPlan
            </template>
            <template v-if="activeTab === 'comments'">
              comments
            </template>
            <template v-if="activeTab === 'changeHistory'">
              changeHistory
            </template>
          </div>
          <input-common ref="commentInputRef"/>
        </div>
      </div>
    </template>
  </detail-drawer>
</template>

<style scoped>
.leftWrapper {
  .header {
    padding: 0 16px;
  }
}
</style>