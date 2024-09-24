<script setup lang="ts">
import FolderAll from '/@/components/o-folder-all/index.vue';
import {useI18n} from "vue-i18n";
import {ProtocolKeyEnum} from "/@/enums/api-enum.ts";
import {computed, nextTick, ref, watch} from "vue";
import {setLocalStorage} from "/@/utils/local-storage.ts";

const props = defineProps<{
  activeFolder?: string; // 选中的节点
  folderName: string; // 名称
  allCount: number; // 总数
  showExpandApi?: boolean; // 展示 展开请求的开关
  notShowOperation?: boolean; // 是否展示操作按钮
  protocolKey: ProtocolKeyEnum;
}>();
const emit = defineEmits<{
  (e: 'setActiveFolder', val: string): void;
  (e: 'changeApiExpand'): void;
  (e: 'selectedProtocolsChange'): void;
}>();
const {t} = useI18n()
const isExpandAll = defineModel<boolean | undefined>('isExpandAll', {
  required: false,
  default: undefined,
});
const isExpandApi = defineModel<boolean>('isExpandApi', {
  required: false,
  default: undefined,
});
const selectedProtocols = ref<string[]>([]);
const visible = ref(false);
const protocolIsEmptyVisible = ref(false);
const allProtocolList = ref<string[]>([]); // 全部
const isCheckedAll = computed(() => {
  return selectedProtocols.value.length === allProtocolList.value.length;
});
const indeterminate = computed(() => {
  return selectedProtocols.value.length > 0 && selectedProtocols.value.length < allProtocolList.value.length;
});
const handleChangeAll = (value: boolean | (string | number | boolean)[]) => {
  if (value) {
    selectedProtocols.value = allProtocolList.value;
  } else {
    selectedProtocols.value = [];
  }
};
const handleGroupChange = (value: (string | number | boolean)[]) => {
  selectedProtocols.value = value as string[];
};
const changeApiExpand = () => {
  isExpandApi.value = !isExpandApi.value;
  nextTick(() => {
    setLocalStorage(`${props.protocolKey}_EXPAND_API`, isExpandApi.value);
    emit('changeApiExpand');
  });
}
watch(
    () => selectedProtocols.value,
    (val) => {
      // 存储取消的项
      const protocols = allProtocolList.value.filter((item) => !val.includes(item as string));
      setLocalStorage(props.protocolKey, protocols);
      emit('selectedProtocolsChange');
      if (props.notShowOperation) return;
      protocolIsEmptyVisible.value = !val.length;
    }
);
watch(
    () => props.notShowOperation,
    (val) => {
      if (val) {
        isExpandAll.value = undefined;
      } else {
        isExpandAll.value = false;
      }
    }
);
defineExpose({
  selectedProtocols,
});
</script>

<template>
  <folder-all v-model:isExpandAll="isExpandAll"
              :active-folder="props.activeFolder"
              :folder-name="props.folderName"
              :all-count="props.allCount"
              @set-active-folder="(val: string) => emit('setActiveFolder', val)">
    <template #expandLeft>
      <n-tooltip>
        <template #trigger>
          <n-button v-show="!props.notShowOperation && showExpandApi" class="!mr-[4px] p-[4px]"  text>
            <template #icon>
              <n-icon>
                <div :class="!isExpandApi?'i-o-icons:eye-show':'i-o-icons:eye-off'"/>
              </n-icon>
            </template>
          </n-button>
        </template>
        {{ !isExpandApi ? t('apiTestManagement.expandApi') : t('apiTestManagement.collapseApi') }}
      </n-tooltip>
    </template>
  </folder-all>
</template>

<style scoped>

</style>