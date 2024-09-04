<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue';
import {computed, ref} from "vue";
import {useI18n} from "vue-i18n";
import {useAppStore} from "/@/store";
import TestPlanTree from "/@/views/test-plan/plan/components/TestPlanTree.vue";
import ButtonIcon from '/@/components/o-button-icon/index.vue'
import {ModuleTreeNode} from "/@/models/common.ts";
import CreateOrEditPlanDrawer from "/@/views/test-plan/plan/components/CreateOrEditPlanDrawer.vue";
import PlanTable from '/@/views/test-plan/plan/components/PlanTable.vue'
import OPopConfirm, {ConfirmValue} from '/@/components/o-popconfirm/index.vue'
import {CreateOrUpdateModule} from "/@/models/case-management/feature-case.ts";
import {useRequest} from "alova/client";
import {createPlanModuleTree} from "/@/api/modules/test-plan/test-plan.ts";

const modulesCount = ref<Record<string, any>>({});
const appStore = useAppStore();
const {t} = useI18n();
const currentProjectId = computed(() => appStore.currentProjectId);
const testPlanTreeRef = ref<InstanceType<typeof TestPlanTree> | null>(null);
const planTableRef = ref<InstanceType<typeof PlanTable> | null>(null);
const offspringIds = ref<string[]>([]);
const nodeName = ref<string>('');
const activeCaseType = ref<'folder' | 'module'>('folder'); // 激活计划树类型
const activeFolder = ref<string>('all');
const isExpandAll = ref(false);
const groupKeyword = ref<string>('');
const options = [
  {
    label: t('common.newCreate'),
    key: 'createPlan'
  },
  {
    label: t('testPlan.testPlanIndex.testPlanGroup'),
    key: 'createGroup'
  },
]
const setActiveFolder = (type: string) => {
  activeFolder.value = type;
};
const selectedKeys = computed({
  get: () => [activeFolder.value],
  set: (val) => val,
});
const expandHandler = () => {
  isExpandAll.value = !isExpandAll.value;
};
const planId = ref<string>();
const addSubVisible = ref(false);
const rootModulesName = ref<string[]>([]);
const folderTree = ref<ModuleTreeNode[]>([]);
const setRootModules = (treeNode: ModuleTreeNode[]) => {
  folderTree.value = treeNode;
  rootModulesName.value = treeNode.map((e) => e.name);
};
const showPlanDrawer = ref<boolean>(false);
const showPlanGroupModel = ref<boolean>(false);
const handleSelect = (key: string) => {
  switch (key) {
    case 'createPlan':
      showPlanDrawer.value = true;
      break;
    case 'createGroup':
      showPlanGroupModel.value = true;
      break;
    default:
      break;
  }
}
const planNodeSelect = (keys: string[], _offspringIds: string[], moduleName: string) => {
  [activeFolder.value] = keys;
  activeCaseType.value = 'module';
  offspringIds.value = [..._offspringIds];
  nodeName.value = moduleName;
}
const {
  send: savePlanModule,
  loading: confirmLoading
} = useRequest((param) => createPlanModuleTree(param), {immediate: false})
const confirmHandler = (formValue: ConfirmValue) => {
  const params: CreateOrUpdateModule = {
    projectId: currentProjectId.value,
    name: formValue.field,
    parentId: 'NONE',
  };
  savePlanModule(params).then(() => {
    testPlanTreeRef.value?.initModules();
    addSubVisible.value = false;
    window.$message.success(t('caseManagement.featureCase.addSuccess'));
  })

}
</script>

<template>
  <o-card simple>
    <n-split :max="0.9" :min="0.1" :default-size="0.2">
      <template #1>
        <div class="min-w-[300px] p-[16px]">
          <div class="mb-[16px] flex justify-between">
            <n-input :placeholder="$t('caseManagement.featureCase.searchTip')"/>
            <n-dropdown class="ml-2" :options="options" :placement="'bottom-start'" :trigger="'click'"
                        @select="handleSelect">
              <n-button text>
                <template #icon>
                  <n-icon size="20">
                    <div class="i-carbon-add-alt"/>
                  </n-icon>
                </template>
              </n-button>
            </n-dropdown>
          </div>
          <div class="test-plan h-[100%]">
            <div class="case h-[38px]">
              <div class="flex items-center" @click="setActiveFolder('all')">
                <div class="i-carbon-folder"/>
                <div class="folder-name mx-[4px]">{{ $t('testPlan.testPlanIndex.allTestPlan') }}</div>
                <div class="folder-count">({{ modulesCount.all || 0 }})</div>
              </div>
              <div class="ml-auto flex items-center">
                <n-tooltip>
                  <template #trigger>
                    <button-icon class="!mr-0 p-[4px]"
                                 :icon-class="isExpandAll?'i-ic-twotone-folder-off':'i-ic-twotone-create-new-folder'"
                                 @click="expandHandler"/>
                  </template>
                  {{ isExpandAll ? t('common.collapseAllSubModule') : t('common.expandAllSubModule') }}
                </n-tooltip>
                <o-pop-confirm
                    v-model:visible="addSubVisible"
                    :title="t('testPlan.testPlanIndex.addSubModule')"
                    :is-delete="false"
                    :all-names="rootModulesName"
                    :ok-text="t('common.confirm')"
                    :loading="confirmLoading"
                    :field-config="{
                                  placeholder: t('testPlan.testPlanIndex.addGroupTip'),
                                  nameExistTipText: t('project.fileManagement.nameExist'),}"
                    @confirm="confirmHandler">
                  <template #trigger>
                    <button-icon v-permission="['PROJECT_TEST_PLAN:READ+ADD']" class="!mr-0 p-[2px]"
                                 icon-class="i-carbon-add-alt"/>
                  </template>
                </o-pop-confirm>
              </div>
            </div>
            <test-plan-tree ref="testPlanTreeRef" v-model:selected-keys="selectedKeys" :is-expand-all="isExpandAll"
                            v-model:groupKeyword="groupKeyword" :active-folder="activeFolder"
                            :modules-count="modulesCount"
                            @init="setRootModules"
                            @plan-tree-node-select="planNodeSelect"
            />
          </div>
        </div>
      </template>
      <template #2>
        <div class="p-[16px]">
          <plan-table ref="planTableRef" :active-folder="activeFolder"
                      :offspring-ids="offspringIds"
                      :active-folder-type="activeCaseType"
                      :modules-count="modulesCount"
                      :module-tree="folderTree"
                      :node-name="nodeName"/>
        </div>
      </template>
    </n-split>
    <create-or-edit-plan-drawer v-model:visible="showPlanDrawer" :plan-id="planId"
                                :module-id="selectedKeys[0]" :module-tree="folderTree"/>
  </o-card>
</template>

<style scoped>
.case {
  padding: 8px 0;
  @apply flex cursor-pointer  items-center justify-between;

  .folder-icon {
    margin-right: 4px;

  }

  .folder-count {
    margin-left: 4px;
  }
}
</style>