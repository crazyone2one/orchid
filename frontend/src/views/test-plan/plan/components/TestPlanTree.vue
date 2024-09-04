<script setup lang="ts">
import OTree from '/@/components/o-tree/index.vue';
import {ModuleTreeNode} from "/@/models/common.ts";
import {computed, h, onBeforeMount, ref, watch} from "vue";
import {useAppStore} from "/@/store";
import {useRequest} from "alova/client";
import {createPlanModuleTree, getTestPlanModule} from "/@/api/modules/test-plan/test-plan.ts";
import {mapTree} from "/@/utils";
import OPopConfirm, {ConfirmValue} from '/@/components/o-popconfirm/index.vue'
import {useI18n} from "vue-i18n";
import ButtonIcon from '/@/components/o-button-icon/index.vue'
import {CreateOrUpdateModule} from "/@/models/case-management/feature-case.ts";
import {TreeNodeData} from "/@/components/o-tree/types.ts";
import type {DropdownOption, TreeOption} from "naive-ui";
import MoreAction from '/@/components/o-table-more-action/index.vue'

const {t} = useI18n()
const testPlanTree = ref<ModuleTreeNode[]>([]);
const props = defineProps<{
  activeFolder?: string; // 当前选中的文件夹，弹窗模式下需要使用
  selectedKeys?: Array<string | number>; // 选中的节点 key
  isExpandAll: boolean; // 是否展开用例节点
  allNames?: string[]; // 所有的模块name列表
  modulesCount?: Record<string, number>; // 模块数量统计对象
  groupKeyword: string;
}>();
const emits = defineEmits([
  'update:selectedKeys',
  'planTreeNodeSelect',
  'init',
  'dragUpdate',
  'getNodeName',
  'update:groupKeyword',
  'deleteNode',
]);
const appStore = useAppStore();
const currentProjectId = computed(() => appStore.currentProjectId);
const selectedNodeKeys = ref(props.selectedKeys || []);
const focusNodeKey = ref<string>('');
const renamePopVisible = ref(false);
const renameCaseName = ref('');

const {loading, send: fetchTestPlanModule} = useRequest((param) => getTestPlanModule(param),
    {
      immediate: false,
      force: ({args}) => !!args[0]
    });
const initModules = (isSetDefaultKey = false) => {
  fetchTestPlanModule({projectId: currentProjectId.value}).then(res => {
    testPlanTree.value = mapTree<ModuleTreeNode>(res, (e) => {
      return {
        ...e,
        hideMoreAction: e.id === 'root',
        draggable: e.id !== 'root',
        count: props.modulesCount?.[e.id] || 0,
      };
    });
    if (isSetDefaultKey) {
      selectedNodeKeys.value = [testPlanTree.value[0].id];
    }
    emits('init', testPlanTree.value, isSetDefaultKey);
  })
}
const resetFocusNodeKey = () => {
  focusNodeKey.value = '';
  renamePopVisible.value = false;
  renameCaseName.value = '';
};
const {
  send: savePlanModule,
  loading: confirmLoading
} = useRequest((param) => createPlanModuleTree(param), {immediate: false})
const addSubModule = (formValue: ConfirmValue, cancel?: () => void) => {
  const params: CreateOrUpdateModule = {
    projectId: currentProjectId.value,
    name: formValue.field,
    parentId: focusNodeKey.value,
  };
  savePlanModule(params).then(() => {
    window.$message.success(t('common.addSuccess'));
    if (cancel) {
      cancel();
    }
    initModules(true);
  })
}
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
const renderSuffix = ({option}: { option: TreeOption }) => {
  return [h(OPopConfirm, {
    title: t('testPlan.testPlanIndex.addSubModule'),
    allNames: (option.children || []).map((e: TreeOption) => e.name || '') as string[],
    okText: t('common.confirm'),
    fieldConfig: {
      placeholder: t('testPlan.testPlanIndex.addGroupTip'),
      nameExistTipText: t('project.fileManagement.nameExist'),
    },
    loading: confirmLoading.value,
    onCancel: () => resetFocusNodeKey(),
    onConfirm: (p1, p2) => addSubModule(p1, p2)
  }, {
    trigger: () => {
      return h(ButtonIcon, {iconClass: 'i-carbon-add-alt'})
    }
  }),
    h(MoreAction, {
      list: caseMoreActions
    }, {})
  ]
}

const planNodeSelect = (selectedKeys: string[], node: TreeNodeData) => {
  const offspringIds: string[] = [];
  mapTree(node.children || [], (e) => {
    offspringIds.push(e.id);
    return e;
  });
  focusNodeKey.value = selectedKeys[0]
  emits('planTreeNodeSelect', selectedKeys, offspringIds, node.name);
}
watch(
    () => props.selectedKeys,
    (val) => {
      selectedNodeKeys.value = val || [];
    }
);
watch(
    () => selectedNodeKeys.value,
    (val) => {
      emits('update:selectedKeys', val);
    }
);
/**
 * 初始化模块文件数量
 */
watch(
    () => props.modulesCount,
    (obj) => {
      testPlanTree.value = mapTree<ModuleTreeNode>(testPlanTree.value, (node) => {
        return {
          ...node,
          count: obj?.[node.id] || 0,
        };
      });
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
  <n-spin :show="loading">
    <o-tree :data="testPlanTree" :selected-keys="props.selectedKeys"
            :render-suffix="renderSuffix"
            @select="planNodeSelect">

    </o-tree>
  </n-spin>
</template>

<style scoped>

</style>