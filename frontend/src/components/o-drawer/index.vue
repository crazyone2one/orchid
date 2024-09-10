<script setup lang="ts">
import {NDrawer, NDrawerContent} from 'naive-ui';
import {ref, watch} from "vue";
import {useI18n} from "vue-i18n";

interface DrawerProps {
  visible: boolean;
  title?: string | undefined;
  titleTag?: string;
  titleTagColor?: string;
  // descriptions?: Description[];
  footer?: boolean;
  mask?: boolean; // 是否显示遮罩
  showSkeleton?: boolean; // 是否显示骨架屏
  okLoading?: boolean;
  okDisabled?: boolean;
  okPermission?: string[]; // 确认按钮权限
  okText?: string;
  cancelText?: string;
  saveContinueText?: string;
  showContinue?: boolean;
  width: string | number; // 抽屉宽度，为数值时才可拖拽改变宽度
  noContentPadding?: boolean; // 是否没有内容内边距
  disabledWidthDrag?: boolean; // 是否禁止拖拽宽度
  closable?: boolean; // 是否显示右上角的关闭按钮
  noTitle?: boolean; // 是否不显示标题栏
  drawerStyle?: Record<string, string>; // 抽屉样式
  showFullScreen?: boolean; // 是否显示全屏按钮
  maskClosable?: boolean; // 点击遮罩是否关闭
  unmountOnClose?: boolean; // 关闭时销毁组件
  handleBeforeCancel?: () => boolean;
}

const props = withDefaults(defineProps<DrawerProps>(), {
  footer: true,
  mask: true,
  closable: true,
  showSkeleton: false,
  showContinue: false,
  disabledWidthDrag: false,
  showFullScreen: false,
  maskClosable: true,
  unmountOnClose: false,
  okPermission: () => [], // 确认按钮权限
});
const emit = defineEmits(['update:visible', 'confirm', 'cancel', 'continue', 'close']);

const {t} = useI18n();

const active = ref(props.visible);
const handleContinue = () => {
  emit('continue');
};

const handleOk = () => {
  emit('confirm');
};

const handleCancel = () => {
  // fullScreen.value?.exitFullscreen();
  active.value = false;
  emit('update:visible', false);
  emit('cancel');
};

const handleClose = () => {
  // fullScreen.value?.exitFullscreen();
  active.value = false;
  emit('update:visible', false);
  emit('close');
};

watch(
    () => props.visible,
    (val) => {
      active.value = val;
    }
);
</script>

<template>
  <n-drawer v-model:show="active" v-bind="props" :class="'ms-drawer'"
            :mask-closable="false"
            @after-leave="handleCancel">
    <n-drawer-content :native-scrollbar="false" closable>
      <template #header>
        <div class="flex items-center justify-between gap-[4px]">
          <slot name="title">
            <div class="flex flex-1 items-center justify-between">
              <div class="flex items-center">
                <n-tooltip :disabled="!props.title">
                  <template #trigger>
                    <span class="one-line-text max-w-[300px]"> {{ props.title }}</span>
                  </template>
                  {{ props.title }}
                </n-tooltip>
                <slot name="headerLeft"></slot>
                <n-tag v-if="titleTag" :bordered="false" :color="props.titleTagColor" class="ml-[8px] mr-auto">
                  {{ props.titleTag }}
                </n-tag>
              </div>
              <slot name="tbutton"></slot>
            </div>
          </slot>
          <div class="right-operation-button-icon">

          </div>
        </div>
      </template>
      <div class="ms-drawer-body">
        <slot></slot>
      </div>
      <template v-if="props.footer" #footer>
        <slot name="footer">
          <div class="flex items-center justify-between">
            <slot name="footerLeft"></slot>
            <div class="ml-auto flex gap-[12px]">
              <n-button :disabled="props.okLoading" @click="handleCancel">
                {{ t(props.cancelText || 'ms.drawer.cancel') }}
              </n-button>
              <n-button v-if="showContinue" secondary v-permission="props.okPermission || []"
                        :disabled="okDisabled">
                {{ t(props.saveContinueText || 'ms.drawer.saveContinue') }}
              </n-button>
              <n-button type="primary" v-permission="props.okPermission || []"
                        :loading="props.okLoading"
                        :disabled="okDisabled" @click="handleOk">
                {{ t(props.okText || 'ms.drawer.ok') }}
              </n-button>
            </div>
          </div>
        </slot>
      </template>
    </n-drawer-content>
  </n-drawer>
</template>

<style scoped>

</style>