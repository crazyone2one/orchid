<script setup lang="ts">
import {ref} from "vue";
import ButtonIcon from '/@/components/o-button-icon/index.vue'
import {FilterFormItem, FilterResult} from "/@/components/o-advance-filter/type.ts";
import {ViewTypeEnum} from "/@/enums/advanced-filter-enum.ts";

const props = defineProps<{
  // rowCount: number;
  filterConfigList: FilterFormItem[]; // 系统字段
  // customFieldsConfigList?: FilterFormItem[]; // 自定义字段
  searchPlaceholder?: string;
  name?: string;
  count?: number;
  notShowInputSearch?: boolean;
  viewType?: ViewTypeEnum;
}>();
const emit = defineEmits<{
  (e: 'keywordSearch', value: string | undefined, combine: FilterResult): void; // keyword 搜索 TODO:可以去除，父组件通过 v-model:keyword 获取关键字
  (e: 'advSearch', value: FilterResult): void; // 高级搜索
  (e: 'dataIndexChange', value: string): void; // 高级搜索选项变更
  (e: 'refresh', value: FilterResult): void;
}>();
const keyword = defineModel<string>('keyword', {default: ''});
const visible = ref(false);
const filterCount = ref(0);
const defaultFilterResult: FilterResult = {accordBelow: 'AND', combine: {}};
const filterResult = ref<FilterResult>({...defaultFilterResult});
const handleResetFilter = () => {
  filterResult.value = {...defaultFilterResult};
  emit('advSearch', {...defaultFilterResult});
};
const handleFilter = (filter: FilterResult) => {
  filterResult.value = filter;
  emit('advSearch', filter);
};

const handleRefresh = () => {
  emit('refresh', filterResult.value);
};

const dataIndexChange = (dataIndex: string) => {
  emit('dataIndexChange', dataIndex);
};

const handleClear = () => {
  keyword.value = '';
  emit('keywordSearch', '', filterResult.value);
};

const handleOpenFilter = () => {
  visible.value = !visible.value;
};
</script>

<template>
  <div class="flex flex-row items-center justify-between">
    <slot name="left">
      <div class="flex">
        <n-popover>
          <template #trigger>
            <div class="flex">
              <div class="one-line-text mr-1 max-h-[32px] max-w-[300px]">
                {{ props.name }}
              </div>
              <span> ({{ props.count }})</span>
            </div>
            <div class="max-w-[400px] text-[14px] font-medium">
              {{ props.name }}
              <span>({{ props.count }})</span>
            </div>
          </template>
        </n-popover>
        <slot name="nameRight"></slot>
      </div>
    </slot>
    <div class="flex flex-row gap-[12px]">
      <n-input v-if="!props.notShowInputSearch" v-model:value="keyword" size="small"
               :placeholder="props.searchPlaceholder" class="w-[240px]"
               clearable
               @clear="handleClear"
               @keyup="emit('keywordSearch', keyword, filterResult)"/>
      <slot name="right"></slot>
      <button-icon icon-class="i-ic-baseline-autorenew"/>
    </div>
  </div>
</template>

<style scoped>

</style>