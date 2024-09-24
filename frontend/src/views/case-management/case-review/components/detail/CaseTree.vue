<script setup lang="ts">
import FolderAll from '/@/components/o-folder-all/index.vue';
import OTree from '/@/components/o-tree/index.vue'
import {computed, onBeforeMount, ref, watch} from "vue";
import {useI18n} from "vue-i18n";
import {useRoute} from "vue-router";
import useCaseReviewStore from "/@/store/modules/case/case-review.ts";
import {useVModel} from "@vueuse/core";
import {mapTree} from "/@/utils";
import {ModuleTreeNode} from "/@/models/common.ts";
import {TreeNodeData} from "/@/components/o-tree/types.ts";

const props = defineProps<{
  modulesCount?: Record<string, number>; // 模块数量统计对象
  showType?: string; // 显示类型
  isExpandAll?: boolean; // 是否展开所有节点
  selectedKeys: string[]; // 选中的节点 key
}>();
const emit = defineEmits(['folderNodeSelect']);
const route = useRoute();
const {t} = useI18n();
const caseReviewStore = useCaseReviewStore();
const moduleKeyword = ref('');
const folderTree = computed(() => caseReviewStore.moduleTree);
const loading = computed(() => caseReviewStore.loading);
const selectedKeys = useVModel(props, 'selectedKeys', emit);
const activeFolder = ref<string>('all');
const allCount = ref(0);
const isExpandAll = ref(props.isExpandAll);
const initModules = () => {
  caseReviewStore.initModules(route.query.id as string)
}
const setActiveFolder = (id: string) => {
  activeFolder.value = id;
  emit('folderNodeSelect', [id], []);
}
const folderNodeSelect = (_selectedKeys: (string | number)[], node: TreeNodeData) => {

}
const selectParentNode = (tree: ModuleTreeNode[]) => {
  mapTree(tree || [], (e) => {
    if (e.id === selectedKeys.value[0]) {
      if (e.parentId) {
        selectedKeys.value = [e.parentId];
        folderNodeSelect([e.parentId], e.parent);
      } else {
        setActiveFolder('all');
      }
      return e;
    }
    return e;
  });
}
watch(
    () => props.isExpandAll,
    (val) => {
      isExpandAll.value = val;
    }
);
onBeforeMount(() => {
  initModules();
});
defineExpose({
  initModules,
  setActiveFolder,
  selectParentNode,
});
</script>

<template>
  <div>
    <n-input v-model:value="moduleKeyword" :placeholder="$t('caseManagement.caseReview.folderSearchPlaceholder')"/>
    <folder-all :active-folder="activeFolder"
                :folder-name="$t('caseManagement.caseReview.allCases')"
                :all-count="allCount"
                @set-active-folder="setActiveFolder"/>
    <n-spin class="min-h-[200px] w-full" :show="loading">
      <o-tree :data="folderTree" :keyword="moduleKeyword" :selected-keys="selectedKeys"
              :default-expand-all="isExpandAll"
              :expand-all="isExpandAll" @select="folderNodeSelect"/>
    </n-spin>
  </div>
</template>

<style scoped>

</style>