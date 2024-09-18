<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue';
import useFeatureCaseStore from "/@/store/modules/case/feature-case.ts";
import {computed, ref, resolveDirective} from "vue";
import ButtonIcon from "/@/components/o-button-icon/index.vue";
import CaseTree from "/@/views/case-management/case-management-feature/components/CaseTree.vue";
import PopConfirm, {ConfirmValue} from '/@/components/o-popconfirm/index.vue'
import {CreateOrUpdateModule} from "/@/models/case-management/feature-case.ts";
import {useAppStore} from "/@/store";
import {useI18n} from "vue-i18n";
import {useRequest} from "alova/client";
import {createCaseModuleTree} from "/@/api/modules/case-management/feature-case.ts";
import CaseTable from "/@/views/case-management/case-management-feature/components/CaseTable.vue";

const featureCaseStore = useFeatureCaseStore();
const modulesCount = computed(() => {
  return featureCaseStore.modulesCount;
});
const appStore = useAppStore()
const {t} = useI18n();
const currentProjectId = computed(() => appStore.currentProjectId);

const isExpandAll = ref(false);
const groupKeyword = ref<string>('');
const caseTreeRef = ref<InstanceType<typeof CaseTree> | null>(null);
const caseTableRef = ref<InstanceType<typeof CaseTable> | null>(null);
const addSubVisible = ref(false);
const permission = resolveDirective('permission')
const activeCaseType = ref<'folder' | 'module'>('folder'); // 激活用例类型
const rootModulesName = ref<string[]>([]); // 根模块名称列表
const setRootModules = (names: string[]) => {
  rootModulesName.value = names;
}
const activeFolder = ref<string>(featureCaseStore.moduleId[0] || 'all');
const offspringIds = ref<string[]>([]);
const activeFolderName = ref('');
const handleCaseNodeSelect = (keys: string[], _offspringIds: string[], moduleName: string) => {
  [activeFolder.value] = keys;
  activeCaseType.value = 'module';
  offspringIds.value = [..._offspringIds];
  featureCaseStore.setModuleId(keys);
  activeFolderName.value = moduleName;
}
const {
  send: saveCaseModule,
  loading: confirmLoading
} = useRequest((param) => createCaseModuleTree(param), {immediate: false})
const handleConfirm = (formValue: ConfirmValue) => {
  const params: CreateOrUpdateModule = {
    projectId: currentProjectId.value,
    name: formValue.field,
    parentId: 'NONE',
  };
  saveCaseModule(params).then(() => {
    window.$message.success(t('caseManagement.featureCase.addSuccess'));
    caseTreeRef.value?.initModules();
    addSubVisible.value = false;
  })
}
</script>

<template>
  <o-card simple>
    <n-split :max="0.9" :min="0.1" :default-size="0.2">
      <template #1>
        <div class="p-[16px] pb-0">
          <div class="feature-case h-[100%]">
            <n-input v-model:value="groupKeyword" :placeholder="$t('caseManagement.caseReview.folderSearchPlaceholder')"
                     clearable :maxlength="255"
                     class="mb-[16px]"/>
            <div class="case h-[38px]">
              <div class="flex items-center">
                <div class="i-carbon-folder"/>
                <div class="folder-name mx-[4px]">{{ $t('caseManagement.featureCase.allCase') }}</div>
                <div class="folder-count">({{ modulesCount.all || 0 }})</div>
              </div>
              <div class="ml-auto flex items-center">
                <n-tooltip>
                  <template #trigger>
                    <button-icon class="!mr-0 p-[4px]"
                                 :icon-class="isExpandAll?'i-ic-twotone-folder-off':'i-ic-twotone-create-new-folder'"
                                 @click="isExpandAll = !isExpandAll"/>
                  </template>
                  {{ isExpandAll ? $t('common.collapseAllSubModule') : $t('common.expandAllSubModule') }}
                </n-tooltip>
                <pop-confirm v-model:visible="addSubVisible" :title="$t('caseManagement.featureCase.addSubModule')"
                             :is-delete="false" :all-names="rootModulesName" :ok-text="$t('common.confirm')"
                             :field-config="{
                    placeholder: $t('caseManagement.featureCase.addGroupTip'),
                    nameExistTipText: $t('project.fileManagement.nameExist'),
                  }"
                             :loading="confirmLoading"
                             @confirm="handleConfirm">
                  <template #trigger>
                    <button-icon v-permission="['FUNCTIONAL_CASE:READ+ADD']" class="!mr-0 p-[2px]"
                                 icon-class="i-carbon-add-alt"/>
                  </template>
                </pop-confirm>
              </div>
            </div>
          </div>
          <div class="h-[calc(100vh-220px)]">
            <case-tree ref="caseTreeRef" :is-expand-all="isExpandAll" v-model:group-keyword="groupKeyword"
                       @init="setRootModules"
                       @case-node-select="handleCaseNodeSelect"
            />
          </div>
        </div>
      </template>
      <template #2>
        <div class="h-full p-[16px_16px]">
          <case-table ref="caseTableRef" :active-folder="activeFolder"
                      :offspring-ids="offspringIds"
                      :modules-count="modulesCount"
                      :module-name="activeFolderName"
                      @set-active-folder="setActiveFolder('all')"/>
        </div>
      </template>
    </n-split>
  </o-card>
</template>

<style scoped>
.case {
  padding: 8px 4px;
  @apply flex cursor-pointer items-center justify-between;

  &:hover {
    background-color: rgb(232, 243, 255);
  }

  .folder-name {
    color: rgb(29, 33, 41);
  }

  .folder-count {
    margin-left: 4px;
    color: rgb(201, 205, 212);
  }
}
</style>