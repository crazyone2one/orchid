<script setup lang="ts">
import ODrawer from '/@/components/o-drawer/index.vue'
import {nextTick, ref, watch} from "vue";
import type {PaginationInfo} from "/@/components/o-pagination/types.ts";
import PrevNextButton from '/@/components/o-prev-next-button/index.vue'

const props = defineProps<{
  visible: boolean;
  title: string;
  width: number;
  detailId: string; // 详情 id
  tooltipText?: string; // tooltip内容
  detailIndex?: number; // 详情 下标
  tableData?: any[]; // 表格数据
  pagination?: PaginationInfo; // 分页器对象
  showFullScreen?: boolean; // 是否显示全屏按钮
  pageChange?: (page: number) => void; // 分页变更函数
  getDetailFunc: any; // 获取详情的请求函数
}>();

const emit = defineEmits(['update:visible', 'loaded', 'loadingDetail', 'getDetail']);
const innerVisible = ref(false);
const loading = ref(false);
const detail = ref<any>({});
const prevNextButtonRef = ref<InstanceType<typeof PrevNextButton>>();
const setDetailLoading = () => {
  emit('loadingDetail');
}
const initDetail = (id?: string) => {
  prevNextButtonRef.value?.initDetail(id);
}
const openPrevDetail = () => {
  prevNextButtonRef.value?.openPrevDetail();
}
const openNextDetail = () => {
  prevNextButtonRef.value?.openNextDetail();
}
const handleDetailLoaded = (val: any) => {
  detail.value = val;
  emit('loaded', val);
}
const handleClose = () => {
  emit('update:visible', false)
}
watch([() => innerVisible.value, () => props.detailId], () => {
  if (innerVisible.value) {
    nextTick(() => {
      // 为了确保 prevNextButtonRef 已渲染
      if (props.tableData && props.pagination && props.pageChange) {
        initDetail();
      } else {
        emit('getDetail');
      }
    });
  }
})
watch(
    () => props.visible,
    (val) => {
      innerVisible.value = val;
    }
);

watch(
    () => innerVisible.value,
    (val) => {
      emit('update:visible', val);
    }
);
defineExpose({
  initDetail,
  openPrevDetail,
  openNextDetail,
});
</script>

<template>
  <o-drawer :visible="innerVisible" :width="props.width" :footer="false" @cancel="handleClose">
    <template #title>
      <div class="flex flex-1 items-center">
        <n-tooltip>
          <template #trigger>
            <slot name="titleName">
              <div class="one-line-text max-w-[300px]">
                {{ props.title }}
              </div>
            </slot>
          </template>
          {{ props.tooltipText ? props.tooltipText : props.title }}
        </n-tooltip>
        <div class="ml-4 flex items-center">
          <slot name="titleLeft" :loading="loading" :detail="detail"></slot>
        </div>
        <prev-next-button ref="prevNextButtonRef" v-if="props.tableData && props.pagination && props.pageChange"
                          v-model:loading="loading" class="ml-[16px]"
                          :detail-id="props.detailId"
                          :detail-index="props.detailIndex"
                          :table-data="props.tableData"
                          :pagination="props.pagination"
                          :page-change="props.pageChange"
                          :get-detail-func="props.getDetailFunc"
                          @loading-detail="setDetailLoading"
                          @loaded="handleDetailLoaded"
        />
        <div class="ml-auto flex items-center">
          <slot name="titleRight" :loading="loading" :detail="detail"></slot>
        </div>
      </div>
    </template>
    <slot :loading="loading" :detail="detail"></slot>
  </o-drawer>
</template>

<style scoped>

</style>