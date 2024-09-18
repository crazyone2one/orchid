<script setup lang="ts">
import {useVModel} from "@vueuse/core";
import OTree from '/@/components/o-tree/index.vue';
import {ModuleTreeNode} from "/@/models/common.ts";
import {computed, h, onBeforeMount, ref, watch} from "vue";
import {useRequest} from "alova/client";
import {createCaseModuleTree, getCaseModuleTree} from "/@/api/modules/case-management/feature-case.ts";
import {useAppStore} from "/@/store";
import {mapTree} from "/@/utils";
import useFeatureCaseStore from "/@/store/modules/case/feature-case.ts";
import type {DropdownOption, TreeOption} from "naive-ui";
import OPopConfirm, {ConfirmValue} from "/@/components/o-popconfirm/index.vue";
import ButtonIcon from "/@/components/o-button-icon/index.vue";
import MoreAction from "/@/components/o-table-more-action/index.vue";
import {useI18n} from "vue-i18n";
import {CreateOrUpdateModule} from "/@/models/case-management/feature-case.ts";
import {TreeNodeData} from "/@/components/o-tree/types.ts";

const props = defineProps<{
  isModal?: boolean; // 是否是弹窗模式
  activeFolder?: string; // 当前选中的文件夹，弹窗模式下需要使用
  selectedKeys?: Array<string | number>; // 选中的节点 key
  isExpandAll: boolean; // 是否展开用例节点
  allNames?: string[]; // 所有的模块name列表
  modulesCount?: Record<string, number>; // 模块数量统计对象
  groupKeyword: string; // 搜索关键字
}>();

const emits = defineEmits([
  'update:selectedKeys',
  'caseNodeSelect',
  'init',
  'dragUpdate',
  'update:groupKeyword',
  'deleteNode',
]);
const {t} = useI18n()
const appStore = useAppStore()
const featureCaseStore = useFeatureCaseStore()
const currentProjectId = computed(() => appStore.currentProjectId);
const moduleKeyword = useVModel(props, 'groupKeyword', emits);
const caseTree = ref<ModuleTreeNode[]>([]);
const selectedNodeKeys = ref(props.selectedKeys || []);
const {send: fetchCaseTree, loading} = useRequest(() => getCaseModuleTree(currentProjectId.value), {
  immediate: false,
  force: true
})
const initModules = (isSetDefaultKey = false) => {
  fetchCaseTree().then(res => {
    caseTree.value = mapTree(res, (e) => {
      return {
        ...e,
        hideMoreAction: e.id === 'root' || props.isModal,
        draggable: e.id !== 'root' && !props.isModal,
        count: props.modulesCount?.[e.id] || 0,
      };
    })
    featureCaseStore.setModulesTree(caseTree.value);
    if (!featureCaseStore.moduleId) {
      featureCaseStore.setModuleId(['all']);
    }
    if (isSetDefaultKey) {
      selectedNodeKeys.value = [caseTree.value[0].id];
      const offspringIds: string[] = [];
      mapTree(caseTree.value[0].children || [], (e) => {
        offspringIds.push(e.id);
        return e;
      });

      emits('caseNodeSelect', selectedNodeKeys.value, offspringIds);
    }
    emits('init', caseTree.value.map((e) => e.name));
  })
}

const focusNodeKey = ref<string>('');
const renamePopVisible = ref(false);
const renameCaseName = ref('');
const resetFocusNodeKey = () => {
  focusNodeKey.value = '';
  renamePopVisible.value = false;
  renameCaseName.value = '';
};
const caseMoreActions: DropdownOption[] = [
  {
    label: t('caseManagement.featureCase.rename'),
    key: 'rename',
    permission: ['PROJECT_TEST_PLAN:READ+UPDATE'],
  },
  {
    label: t('caseManagement.featureCase.delete'),
    key: 'delete',
    danger: true,
    permission: ['PROJECT_TEST_PLAN:READ+DELETE'],
  },
];
const {
  send: saveCaseModule,
  loading: confirmLoading
} = useRequest((param) => createCaseModuleTree(param), {immediate: false})
const addSubModule = (formValue: ConfirmValue, cancel?: () => void) => {
  const params: CreateOrUpdateModule = {
    projectId: currentProjectId.value,
    name: formValue.field,
    parentId: focusNodeKey.value,
  };
  saveCaseModule(params).then(() => {
    window.$message.success(t('common.addSuccess'));
    if (cancel) {
      cancel();
    }
    initModules(true);
  })
}
const setFocusKey = (option: TreeOption) => {
  focusNodeKey.value = option.id as string || '';
}
const renderSuffix = ({option}: { option: TreeOption }) => {
  return [h(OPopConfirm, {
    title: t('caseManagement.featureCase.addSubModule'),
    allNames: (option.children || []).map((e: TreeOption) => e.name || '') as string[],
    okText: t('common.confirm'),
    fieldConfig: {
      placeholder: t('caseManagement.featureCase.addGroupTip'),
      nameExistTipText: t('project.fileManagement.nameExist'),
    },
    loading: confirmLoading.value,
    onCancel: () => resetFocusNodeKey(),
    onConfirm: (p1, p2) => addSubModule(p1, p2)
  }, {
    trigger: () => {
      return h(ButtonIcon, {iconClass: 'i-carbon-add-alt', onClick: () => setFocusKey(option)})
    }
  }),
    h(MoreAction, {
      list: caseMoreActions
    }, {})
  ]
}
const caseNodeSelect = (selectedKeys: string[], node: TreeNodeData) => {
  const offspringIds: string[] = [];
  mapTree(node.children || [], (e) => {
    offspringIds.push(e.id);
    return e;
  });
  focusNodeKey.value = selectedKeys[0]
  emits('caseNodeSelect', selectedKeys, offspringIds, node.name);
}
watch(
    () => props.selectedKeys,
    (val) => {
      selectedNodeKeys.value = val || [];
      featureCaseStore.setModuleId(val as string[]);
    }
);

watch(
    () => selectedNodeKeys.value,
    (val) => {
      emits('update:selectedKeys', val);
    }
);
onBeforeMount(() => {
  initModules();
});

defineExpose({
  initModules,
});
</script>

<template>
  <n-spin :show="loading" class="w-full">
    <n-input
        v-if="props.isModal"
        v-model:model-value="moduleKeyword"
        :placeholder="$t('caseManagement.caseReview.folderSearchPlaceholder')"
        clearable
        class="mb-[16px]"
        :max-length="255"
    />
    <o-tree :data="caseTree" :keyword="moduleKeyword" :selected-keys="props.selectedKeys"
            :expand-all="props.isExpandAll"
            :render-suffix="renderSuffix"
            @select="caseNodeSelect"/>
  </n-spin>
</template>

<style scoped>

</style>