<script setup lang="ts">
import {h, nextTick, onBeforeMount, ref, Ref, VNodeChild, watch} from "vue";
import {NTree, TreeOption} from "naive-ui";
import {TreeNodeData} from "/@/components/o-tree/types.ts";
import {mapTree, traverseTree} from "/@/utils";
import {cloneDeep} from "lodash-es";

const props = withDefaults(defineProps<{
  keyword?: string;
  renderSuffix?: (info: { option: TreeNodeData, checked: boolean, selected: boolean }) => VNodeChild;
  useMapData?: boolean;
  defaultExpandAll?: boolean;
  searchDebounce?: number; // 搜索防抖 ms 数
  isModal?: boolean;
}>(), {
  useMapData: true,
  defaultExpandAll: false,
  searchDebounce: 300,
});
const treeRef = ref();
const filterTreeData = ref<TreeNodeData[]>([]); // 初始化时全量的树数据或在非搜索情况下更新后的全量树数据
const treeContainerRef: Ref = ref(null);
const data = defineModel<TreeNodeData[]>('data', {
  required: true,
});
const emit = defineEmits<{
  (e: 'select', selectedKeys: Array<string>, node: TreeOption): void;
  // (
  //     e: 'drop',
  //     tree: TreeNodeData[],
  //     dragNode: TreeNodeData, // 被拖拽的节点
  //     dropNode: TreeNodeData, // 放入的节点
  //     dropPosition: number // 放入的位置，-1 为放入节点前，1 为放入节点后，0 为放入节点内
  // ): void;
  // (e: 'moreActionSelect', item: ActionsItem, node: TreeNodeData): void;
  // (e: 'moreActionsClose'): void;
  // (e: 'check', val: Array<string | number>, node: TreeNodeData): void;
  // (e: 'expand', node: MsTreeExpandedData, _expandKeys: Array<string | number>): void;
}>();
const selectedKeys = defineModel<(string | number)[]>('selectedKeys', {
  default: [],
});
const checkedKeys = defineModel<(string | number)[]>('checkedKeys', {
  default: [],
});
const indeterminateKeys = defineModel<(string | number)[]>('indeterminateKeys', {
  default: [],
});
const focusNodeKey = defineModel<string | number>('focusNodeKey', {
  default: '',
});
const expandKeys = ref<Set<string | number>>(new Set([]));
const updateCheckedKeys = (keys: Array<string | number>, options: Array<TreeNodeData | null>,
                           meta: {
                             node: TreeNodeData | null
                             action: 'check' | 'uncheck'
                           }) => {
  console.log('updateCheckedKeys', keys, options, meta)
}
const loop = (data: TreeNodeData[], key: string, callback: (item: TreeNodeData, index: number, arr: TreeNodeData[]) => void) => {
  data.some((item, index, arr) => {
    if (item.id === key) {
      callback(item, index, arr);
      return true;
    }
    if (item.children) {
      return loop(item.children, key, callback);
    }
    return false;
  })
}
const nodeProps = ({option}: { option: TreeNodeData }) => {
  return {
    onClick() {
      const _selectedKeys = [option.id]
      const selectNode: TreeOption = cloneDeep(option);
      loop(data.value, selectNode.id as string, (item) => {
        selectNode.children = item.children;
      });
      emit('select', _selectedKeys, selectNode)
    }
  }
}
const renderLabel = ({option}: { option: TreeOption }) => {
  return h('div', {class: 'inline-flex w-full gap-[8px]'}, {
    default: () => [
      h('div', {class: 'one-line-text'}, {default: () => option.name}),
      h('div', {class: 'ml-[4px] hover:hidden'}, `(${option.count})` || 0)
    ]
  })
}
onBeforeMount(() => {
  if (props.useMapData) {
    filterTreeData.value = mapTree(data.value, (node) => {
      node.expanded = props.defaultExpandAll;
      return node;
    });
  } else {
    traverseTree(data.value, (node) => {
      node.expanded = props.defaultExpandAll;
    });
    filterTreeData.value = data.value;
  }
  nextTick(() => {
    if (props.defaultExpandAll && treeRef.value) {
      treeRef.value.expandAll(true);
    }
  });
});
watch(() => data.value, (value) => {
  if (!props.keyword) {
    if (props.useMapData) {
      filterTreeData.value = mapTree(value, (node) => {
        node.expanded = expandKeys.value.has(node['id']);
        return node;
      })
    } else {
      traverseTree(value, (node) => {
        node.expanded = expandKeys.value.has(node['id']);
      });
      filterTreeData.value = value;
    }
  }
}, {
  deep: true,
})
</script>

<template>
  <div ref="treeContainerRef" :class="['ms-tree-container']">
    <n-tree
        v-model:checked-keys="checkedKeys"
        v-model:selected-keys="selectedKeys"
        v-model:indeterminate-keys="indeterminateKeys"
        block-line
        cascade
        checkable
        :selectable="false"
        :data="filterTreeData"
        :render-suffix="renderSuffix"
        :render-label="renderLabel"
        label-field="name"
        key-field="id"
        class="ms-tree"
        :default-expand-all="props.defaultExpandAll"
        @update:checked-keys="updateCheckedKeys"
        :node-props="nodeProps"
    />
  </div>
</template>

<style scoped>
.ms-tree-container{
  @apply h-full;
  .ms-tree{
    @apply h-full ;
  }
}
</style>