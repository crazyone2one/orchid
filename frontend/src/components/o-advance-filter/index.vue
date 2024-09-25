<script setup lang="ts">
import {computed, onMounted, ref} from "vue";
import ButtonIcon from '/@/components/o-button-icon/index.vue'
import {FilterFormItem, FilterResult, ViewItem} from "/@/components/o-advance-filter/type.ts";
import {ViewTypeEnum} from "/@/enums/advanced-filter-enum.ts";
import FilterDrawer from "/@/components/o-advance-filter/FilterDrawer.vue";
import {useRequest} from "alova/client";
import {getViewList} from "/@/api/modules/setting/user.ts";
import {useAppStore} from "/@/store";
import {getProjectOptions} from "/@/api/modules/project-management/project-member.ts";
import {useI18n} from "vue-i18n";

const props = defineProps<{
  filterConfigList: FilterFormItem[]; // 系统字段
  customFieldsConfigList?: FilterFormItem[]; // 自定义字段
  searchPlaceholder?: string;
  name?: string;
  count?: number;
  notShowInputSearch?: boolean;
  viewType?: ViewTypeEnum;
}>();
const emit = defineEmits<{
  (e: 'keywordSearch', value: string | undefined): void; // keyword 搜索 TODO:可以去除，父组件通过 v-model:keyword 获取关键字
  (e: 'advSearch', value: FilterResult, viewId: string, isAdvancedSearchMode: boolean): void; // 高级搜索
  (e: 'refresh', value: FilterResult): void;
}>();
const {t} = useI18n()
const keyword = defineModel<string>('keyword', {default: ''});
const filterDrawerRef = ref<InstanceType<typeof FilterDrawer>>();
const visible = ref(false);
const filterCount = ref(0);
const currentView = ref(''); // 当前视图
const internalViews = ref<ViewItem[]>([]);
const customViews = ref<ViewItem[]>([]);
const appStore = useAppStore()
const filterResult = ref<FilterResult>({searchMode: 'AND', conditions: []});
const isAdvancedSearchMode = ref(false);
const allViewNames = computed(() => [...internalViews.value, ...customViews.value].map((item) => item.name));
const canNotAddView = computed(() => customViews.value.length >= 10);
const memberOptions = ref<{ label: string; value: string }[]>([]);
const handleFilter = (filter: FilterResult) => {
  keyword.value = '';
  const haveConditions: boolean =
      filter.conditions?.some((item) => {
        const valueCanEmpty = ['EMPTY', 'NOT_EMPTY'].includes(item.operator as string);
        const isValidValue = typeof item.value !== 'number' ? item.value?.length : item.value;
        return valueCanEmpty || isValidValue;
      }) ?? false;
  // 开启高级筛选：非默认视图或有筛选条件
  isAdvancedSearchMode.value = currentView.value !== internalViews.value[0].id || haveConditions;
  filterResult.value = filter;
  emit('advSearch', filter, currentView.value, isAdvancedSearchMode.value);
};

const handleRefresh = () => {
  emit('refresh', filterResult.value);
};

const handleClear = () => {
  keyword.value = '';
  emit('keywordSearch', '');
};

const handleOpenFilter = () => {
  visible.value = !visible.value;
};
const clearFilter = () => {
  if (currentView.value === internalViews.value[0].id) {
    filterDrawerRef.value?.handleReset();
    handleFilter({searchMode: 'AND', conditions: []});
  } else {
    currentView.value = internalViews.value[0].id;
  }
}
const {send: fetchMemberOptions} = useRequest(() => getProjectOptions(appStore.currentProjectId), {
  immediate: false,
  force: true
});
const getMemberOptions = () => {
  fetchMemberOptions().then(res => {
    memberOptions.value = [{name: t('common.currentUser'), id: 'CURRENT_USER'}, ...res].map((e: any) => ({
      label: e.name,
      value: e.id,
    }));
  })
}
const getUserViewList = async () => {
  const res = await getViewList(props.viewType as ViewTypeEnum, appStore.currentProjectId);
  internalViews.value = res.internalViews;
  customViews.value = res.customViews;
}
const changeViewToFirstCustom = () => {
  getUserViewList()
  currentView.value = customViews.value[0].id;
}
const viewSelectOptionVisible = ref(false);
const changeView = (item: ViewItem) => {
  currentView.value = item.id;
  viewSelectOptionVisible.value = false;
}
onMounted(async () => {
  if (props.viewType) {
    getMemberOptions();
    await getUserViewList();
    currentView.value = internalViews.value[0]?.id;
  }
});
defineExpose({
  isAdvancedSearchMode,
});
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
          </template>
          <div class="max-w-[400px] text-[14px] font-medium">
            {{ props.name }}
            <span>({{ props.count }})</span>
          </div>
        </n-popover>
        <slot name="nameRight"></slot>
      </div>
    </slot>
    <div class="flex flex-row gap-[8px]">
      <n-input v-if="!props.notShowInputSearch" v-model:value="keyword" size="small"
               :placeholder="props.searchPlaceholder" class="w-[187px]"
               clearable
               @clear="handleClear"
               @keyup="emit('keywordSearch', keyword)"/>
      <n-button v-if="props.viewType" :secondary="isAdvancedSearchMode" :class="`p-[0_8px]`" @click="handleOpenFilter">
        <template #icon>
          <div class="i-carbon-filter" :class="`${isAdvancedSearchMode ? 'text-blue-600' : 'text-gray'}`"/>
        </template>
      </n-button>
      <n-button v-show="isAdvancedSearchMode" text @click="clearFilter"> {{
          $t('advanceFilter.clearFilter')
        }}
      </n-button>
      <slot name="right"></slot>
      <button-icon icon-class="i-ic-baseline-autorenew" @click="handleRefresh"/>
    </div>
  </div>
  <filter-drawer ref="filterDrawerRef"
                 v-model:visible="visible"
                 :current-view="currentView"
                 :view-type="props.viewType ?? ViewTypeEnum.FUNCTIONAL_CASE"
                 :internal-views="internalViews"
                 :all-view-names="allViewNames"
                 :config-list="props.filterConfigList"
                 :custom-list="props.customFieldsConfigList"
                 :can-not-add-view="canNotAddView"
                 :member-options="memberOptions"
                 @handle-filter="handleFilter"
                 @refresh-view-list="getUserViewList"
                 @change-view-to-first-custom="changeViewToFirstCustom"
  />
</template>

<style scoped>

</style>