<script setup lang="ts">
import {useI18n} from "vue-i18n";
import {computed, ref, watch} from "vue";
import type {PaginationInfo} from '/@/components/o-pagination/types.ts'

const props = defineProps<{
  loading: boolean;
  detailId: string; // 详情 id
  detailIndex?: number; // 详情 下标
  tableData: any[]; // 表格数据
  pagination: PaginationInfo; // 分页器对象
  pageChange: (page: number) => void; // 分页变更函数
  getDetailFunc: any; // 获取详情的请求函数
}>();

const emit = defineEmits(['update:loading', 'loaded', 'loadingDetail']);
const {t} = useI18n();

const innerLoading = ref(false);
const activeDetailId = ref<string>(props.detailId);
const activeDetailIndex = ref(props.detailIndex || 0);
// 当前查看的是否是总数据的第一条数据，用当前查看数据的下标是否等于0，且当前页码是否等于1
const activeDetailIsFirst = computed(() => activeDetailIndex.value === 0 && props.pagination.page === 1);
const activeDetailIsLast = computed(
    // 当前查看的是否是总数据的最后一条数据，用(当前页码-1)*每页条数+当前查看的条数下标，是否等于总条数
    () =>
        activeDetailIndex.value === props.tableData.length - 1 &&

        (props.pagination.page - 1) * props.pagination.pageSize + (activeDetailIndex.value + 1) >=

        props.pagination.total
);
const initDetail = async (id?: string) => {
  innerLoading.value = true;
  emit('loadingDetail');
  const res = await props.getDetailFunc(id || activeDetailId.value);
  emit('loaded', res);
}
const openPrevDetail = async () => {
  if (!activeDetailIsFirst.value) {
    if (activeDetailIndex.value === 0 && props.pagination) {
      try {
        // 当前查看的是当前页的第一条数据，则需要加载上一页的数据
        innerLoading.value = true;
        await props.pageChange(props.pagination.page - 1);
        activeDetailId.value = props.tableData[props.tableData.length - 1].id;
        activeDetailIndex.value = props.tableData.length - 1;
      } catch (error) {
        console.log(error);
        innerLoading.value = false;
      }
    } else {
      // 当前查看的不是当前页的第一条数据，则直接查看上一条数据
      activeDetailId.value = props.tableData[activeDetailIndex.value - 1].id;
      activeDetailIndex.value -= 1;
    }
    initDetail();
  }
}
const openNextDetail = async () => {
  if (!activeDetailIsLast.value) {
    if (activeDetailIndex.value === props.tableData.length - 1 && props.pagination) {
      try {
        // 当前查看的是当前页的最后一条数据，则需要加载下一页的数据
        innerLoading.value = true;
        await props.pageChange(props.pagination.page + 1);
        activeDetailId.value = props.tableData[0].id;
        activeDetailIndex.value = 0;
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
        innerLoading.value = false;
      }
    } else {
      // 当前查看的不是当前页的最后一条数据，则直接查看下一条数据
      activeDetailId.value = props.tableData[activeDetailIndex.value + 1].id;
      activeDetailIndex.value += 1;
    }
    initDetail();
  }
}
watch(
    () => props.detailIndex,
    (val) => {
      activeDetailIndex.value = val as number;
    }
);
watch(
    () => props.detailId,
    (val) => {
      activeDetailId.value = val;
    },
    {immediate: true}
);
watch(
    () => props.loading,
    (val) => {
      innerLoading.value = val;
    }
);
watch(
    () => innerLoading.value,
    (val) => {
      emit('update:loading', val);
    }
);
defineExpose({
  initDetail,
  openPrevDetail,
  openNextDetail,
});
</script>

<template>
  <div>
    <n-tooltip>
      <template #trigger>
        <n-button size="tiny" :disabled="activeDetailIsFirst || innerLoading" class="mr-[4px]" @click="openPrevDetail">
          <template #icon>
            <n-icon>
              <div class="i-carbon-caret-left"/>
            </n-icon>
          </template>
        </n-button>
      </template>
      {{ activeDetailIsFirst ? t('ms.prevNextButton.noPrev') : t('ms.prevNextButton.prev') }}
    </n-tooltip>

    <n-tooltip>
      <template #trigger>
        <n-button size="tiny" :disabled="activeDetailIsLast || innerLoading" class="mr-[4px]" @click="openNextDetail">
          <template #icon>
            <n-icon>
              <div class="i-carbon-caret-right"/>
            </n-icon>
          </template>
        </n-button>
      </template>
      {{ activeDetailIsLast ? t('ms.prevNextButton.noNext') : t('ms.prevNextButton.next') }}
    </n-tooltip>
  </div>
</template>

<style scoped>

</style>