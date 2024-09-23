<script setup lang="ts">

import {useRouter} from "vue-router";

const props = withDefaults(defineProps<Partial<{
  simple: boolean; // 简单模式，没有标题和底部栏
  title: string; // 卡片标题
  subTitle: string; // 卡片副标题
  hideContinue: boolean; // 隐藏保存并继续创建按钮
  hideFooter: boolean; // 隐藏底部栏
  loading: boolean; // 卡片 loading 状态
  isEdit: boolean; // 是否编辑状态
  specialHeight: number; // 特殊高度，例如某些页面有面包屑，autoHeight 时无效
  hideBack: boolean; // 隐藏返回按钮
  autoHeight: boolean; // 内容区域高度是否自适应
  autoWidth: boolean; // 内容区域宽度是否自适应
  otherWidth: number; // 该宽度为卡片外部同级容器的宽度
  headerMinWidth: number; // 卡片头部最小宽度
  minWidth: number; // 卡片内容最小宽度
  hasBreadcrumb: boolean; // 是否有面包屑，如果有面包屑，高度需要减去面包屑的高度
  noContentPadding: boolean; // 内容区域是否有padding
  isFullscreen?: boolean; // 是否全屏
  hideDivider?: boolean; // 是否隐藏分割线
  handleBack: () => void; // 自定义返回按钮触发事件
  showFullScreen: boolean; // 是否显示全屏按钮
  saveText?: string; // 保存按钮文案
  saveAndContinueText?: string; // 保存并继续按钮文案
}>>(), {
  simple: false,
  hideContinue: false,
  hideFooter: false,
  isEdit: false,
  specialHeight: 0,
  hideBack: false,
  autoHeight: false,
  autoWidth: false,
  hasBreadcrumb: false,
  noContentPadding: false,
})

const emit = defineEmits(['saveAndContinue', 'save', 'toggleFullScreen']);
const router = useRouter();
const _specialHeight = props.hasBreadcrumb ? 32 + props.specialHeight : props.specialHeight; // 有面包屑的话，默认面包屑高度24+8间距
const handleBack = () => {
  if (typeof props.handleBack === 'function') {
    props.handleBack();
  } else {
    router.back();
  }
}
</script>

<template>
  <n-spin class="z-[100] !block" :show="props.loading">
    <n-card
        :segmented="{
      content: true,
      footer: 'soft',
    }"
    >
      <template #header>
        <slot name="headerLeft">
          <div class="font-medium text-gray-900">{{ props.title }}</div>
          <div class="text-gray-300">{{ props.subTitle }}</div>
        </slot>
      </template>
      <template #header-extra>
        <slot name="headerRight"></slot>
      </template>
      <div class="relative h-full w-full" :style="{ minWidth: `${props.minWidth || 1000}px` }">
        <slot></slot>
      </div>
      <template #footer>
        <div v-if="!props.hideFooter && !props.simple" class="ms-card-footer">
          <div class="ml-0 mr-auto">
            <slot name="footerLeft"></slot>
          </div>
          <slot name="footerRight">
            <div class="flex justify-end gap-[16px]">
              <n-button secondary @click="handleBack">{{ $t('mscard.defaultCancelText') }}</n-button>
              <n-button secondary @click="emit('saveAndContinue')">
                {{ props.saveAndContinueText || $t('mscard.defaultSaveAndContinueText') }}
              </n-button>
              <n-button type="primary" @click="emit('save')">
                {{ props.saveText || $t(props.isEdit ? 'mscard.defaultUpdate' : 'mscard.defaultConfirm') }}
              </n-button>
            </div>
          </slot>
        </div>
      </template>
      <!--    <template #action>-->
      <!--      #action-->
      <!--    </template>-->
    </n-card>
  </n-spin>

</template>

<style scoped>
.ms-card-footer{
  @apply fixed flex justify-between bg-white;
  right: 16px;
  bottom: 0;
  z-index: 100;
  padding: 24px;
  border-bottom: 0;

  --tw-shadow: 0 -1px 4px rgb(2 2 2 / 10%);
}
</style>