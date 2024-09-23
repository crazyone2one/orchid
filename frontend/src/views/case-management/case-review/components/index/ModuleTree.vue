<script setup lang="ts">
import FolderAll from '/src/components/o-folder-all/index.vue'
import {hasAnyPermission} from "/@/utils/permission.ts";
import {ModuleTreeNode} from "/@/models/common.ts";
import {h, onBeforeMount, ref} from "vue";
import PopConfirm from '/src/views/case-management/case-review/components/index/PopConfirm.vue'
import {useAppStore} from "/@/store";
import {useRequest} from "alova/client";
import {getReviewModules} from "/@/api/modules/case-management/case-review.ts";
import {mapTree} from "/@/utils";
import OTree from '/src/components/o-tree/index.vue'
import {TreeNodeData} from "/@/components/o-tree/types.ts";
import type {TreeOption} from "naive-ui";
import ButtonIcon from "/src/components/o-button-icon/index.vue";

const props = defineProps<{
  isModal?: boolean; // 是否是弹窗模式
  modulesCount?: Record<string, number>; // 模块数量统计对象
  isExpandAll?: boolean; // 是否展开所有节点
}>();
const emit = defineEmits<{
  (e: 'init', data: ModuleTreeNode[], nodePathObj: Record<string, any>): void;
  (e: 'folderNodeSelect', selectedKeys: string[], offspringIds: string[]): void;
  (e: 'create'): void;
  (e: 'nodeDelete'): void;
  (e: 'nodeDrop'): void;
}>();
const appStore = useAppStore()
const isExpandAll = ref(props.isExpandAll);
const activeFolder = ref<string>('all');
const allFileCount = ref(0);
const rootModulesName = ref<string[]>([]); // 根模块名称列表
const selectedKeys = ref<string[]>([]);
const folderTree = ref<ModuleTreeNode[]>([]);
const focusNodeKey = ref<string | number>('');
const renamePopVisible = ref(false);
const renameFolderTitle = ref(''); // 重命名的文件夹名称
const setActiveFolder = (id: string) => {
  activeFolder.value = id;
  if (id === 'all') {
    selectedKeys.value = [];
  }
  emit('folderNodeSelect', [id], []);
}
const {
  loading,
  send: fetchReviewModules
} = useRequest(() => getReviewModules(appStore.currentProjectId), {immediate: false, force: true})
const initModules = (isSetDefaultKey = false) => {
  fetchReviewModules().then(res => {
    const nodePathObj = {} as Record<string, any>;
    folderTree.value = mapTree<ModuleTreeNode>(res, (e, fullPath) => {
      // 拼接当前节点的完整路径
      nodePathObj[e.id] = {
        path: e.path,
        fullPath,
      };
      return {
        ...e,
        hideMoreAction: e.id === 'root' || props.isModal,
        draggable: e.id !== 'root' && !props.isModal,
        disabled: e.id === activeFolder.value && props.isModal,
        count: props.modulesCount?.[e.id] || 0, // 避免模块数量先初始化完成了，数量没更新
      };
    });
    if (isSetDefaultKey) {
      selectedKeys.value = [folderTree.value[0].id];
      const offspringIds: string[] = [];
      mapTree(folderTree.value[0].children || [], (e) => {
        offspringIds.push(e.id);
        return e;
      });

      emit('folderNodeSelect', selectedKeys.value, offspringIds);
    }
    emit('init', folderTree.value, nodePathObj);
  })
}
const folderNodeSelect = (selectedKeys: string[], node: TreeNodeData) => {
  const offspringIds: string[] = [];
  mapTree(node.children || [], (e) => {
    offspringIds.push(e.id);
    return e;
  });
  activeFolder.value = selectedKeys[0]
  emit('folderNodeSelect', selectedKeys, offspringIds);
}
const setFocusNodeKey = (node: TreeOption) => {
  focusNodeKey.value = node.id as string || '';
}
const resetFocusNodeKey = () => {
  focusNodeKey.value = '';
  renamePopVisible.value = false;
  renameFolderTitle.value = '';
}
const renderSuffix = ({option}: { option: TreeOption }) => {
  const result = []
  if (option.id !== 'root' && hasAnyPermission(['CASE_REVIEW:READ+ADD'])) {
    result.push(
        h(PopConfirm, {
          mode: 'add',
          allNames: (option.children || []).map((e: TreeOption) => e.name || '') as string[],
          parentId: option.id as string,
          onAddFinish: () => initModules(),
          onClose: () => resetFocusNodeKey()
        }, {
          trigger: () => {
            return h(ButtonIcon, {iconClass: 'i-carbon-add-alt', onClick: () => setFocusNodeKey(option)})
          }
        })
    )
  }
  if (option.id !== 'root' && hasAnyPermission(['CASE_REVIEW:READ+UPDATE'])) {
    result.push(
        h(PopConfirm, {
          mode: 'rename',
          allNames: (option.children || []).map((e: TreeOption) => e.name || '') as string[],
          parentId: option.id as string,
          nodeId: option.id as string,
          onAddFinish: () => initModules(),
          onClose: () => resetFocusNodeKey()
        }, {
          trigger: () => {
            return h('span', {id: `renameSpan${option.id}`, class: 'relative'}, {})
          }
        })
    )
  }
  return result;
}
onBeforeMount(() => {
  initModules();
});
defineExpose({
  initModules,
});
</script>

<template>
  <div>
    <div class="mb-[16px] flex justify-between">
      <n-input :placeholder="$t('caseManagement.caseReview.folderSearchPlaceholder')" :maxlength="255"/>
      <n-button v-if="!props.isModal && hasAnyPermission(['CASE_REVIEW:READ+ADD'])" class="ml-2"
                type="primary" @click="emit('create')">
        {{ $t('common.newCreate') }}
      </n-button>
    </div>
    <folder-all v-if="!props.isModal" v-model:isExpandAll="isExpandAll"
                :active-folder="activeFolder"
                :folder-name="$t('caseManagement.caseReview.allReviews')"
                :all-count="allFileCount"
                @set-active-folder="setActiveFolder">
      <template #expandRight>
        <pop-confirm v-if="hasAnyPermission(['CASE_REVIEW:READ+UPDATE'])"
                     mode="add"
                     :all-names="rootModulesName"
                     parent-id="NONE"
                     @add-finish="() => initModules()">
          <template #trigger>
            <div class="i-carbon-add-alt !mr-0 p-[2px]"/>
          </template>

        </pop-confirm>
      </template>
    </folder-all>
    <n-spin :show="loading" class="min-h-[400px] w-full">
      <o-tree :data="folderTree" :selected-keys="selectedKeys"
              :default-expand-all="isExpandAll"
              :render-suffix="renderSuffix"
              @select="folderNodeSelect">

      </o-tree>
    </n-spin>
  </div>
</template>

<style scoped>

</style>