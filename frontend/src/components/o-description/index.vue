<script setup lang="ts">
import {NSkeleton} from "naive-ui";
import {ref} from "vue";

export interface Description {
  label: string;
  value: (string | number) | (string | number)[];
  key?: string;
  isTag?: boolean; // 是否标签
  tagClass?: string; // 标签自定义类名
  tagType?: 'default' | 'primary' | 'danger' | 'warning' | 'success' | 'link'; // 标签类型
  tagTheme?: 'dark' | 'light' | 'outline' | 'lightOutLine' | 'default'; // 标签主题
  tooltipPosition?:
      | 'top'
      | 'tl'
      | 'tr'
      | 'bottom'
      | 'bl'
      | 'br'
      | 'left'
      | 'lt'
      | 'lb'
      | 'right'
      | 'rt'
      | 'rb'
      | undefined; // 提示位置防止窗口抖动
  tagMaxWidth?: string; // 标签最大宽度
  closable?: boolean; // 标签是否可关闭
  showTagAdd?: boolean; // 是否显示添加标签
  isButton?: boolean;
  showCopy?: boolean;
  copyTimer?: any | null;
  onClick?: () => void;
}

const props = withDefaults(
    defineProps<{
      showSkeleton?: boolean;
      skeletonLine?: number;
      column?: number;
      descriptions: Description[];
      labelWidth?: string;
      oneLineValue?: boolean;
      addTagFunc?: (val: string, item: Description) => Promise<void>;
    }>(),
    {
      column: 1,
    }
);
const emit = defineEmits<{
  (e: 'addTag', val: string): void;
  (e: 'tagClose', tag: string | number, item: Description): void;
}>();
const addTagInput = ref('');
const showTagInput = ref(false);
const inputRef = ref();
const tagInputLoading = ref(false);
const tagInputError = ref('');
const getValueClass = (item: Description) => {
  if (item.isTag) {
    return 'ms-description-item-value--tagline';
  }
  if (props.oneLineValue) {
    return 'ms-description-item-value ms-description-item-value--one-line';
  }
  return 'ms-description-item-value';
}
</script>

<template>
  <n-skeleton v-if="props.showSkeleton" :loading="props.showSkeleton" round size="large"/>
  <div v-else class="ms-description">
    <slot name="title"></slot>
    <div
        v-for="(item, index) of props.descriptions"
        :key="item.label"
        class="ms-description-item"
        :style="{ marginBottom: props.descriptions.length - index <= props.column ? '' : '16px' }"
    >
      <div class="ms-description-item-label one-line-text max-w-[120px]">
        <slot name="item-label">{{ item.label }}</slot>
      </div>
      <div :class="getValueClass(item)">
        <slot name="item-value" :item="item">
          <template v-if="item.isTag">
            <slot name="tag" :item="item">
              <div class="w-[280px] overflow-hidden">

              </div>
              <span v-if="!item.showTagAdd" v-show="Array.isArray(item.value) && item.value.length === 0">-</span>
              <div v-else>
                <template v-if="showTagInput">
                  <n-input
                      v-model:value="addTagInput"
                      ref="inputRef"
                      :maxlength="64"
                  />
                  <span v-if="tagInputError" class="text-[12px] leading-[16px] text-[rgb(var(--danger-6))]">
                    {{ $t('ms.description.addTagRepeat') }}
                  </span>
                </template>
                <n-tag v-else-if="Array.isArray(item.value) && item.value.length < 10">
                  {{ $t('ms.description.addTag') }}
                </n-tag>
              </div>
            </slot>
          </template>
          <n-button v-else-if="item.isButton" text>{{ item.value }}</n-button>
          <template v-else>
            <slot name="value" :item="item">
              <div class="w-[fit-content]">
                {{
                  item.value === undefined || item.value === null || item.value?.toString() === '' ? '-' : item.value
                }}
              </div>
            </slot>
            <template v-if="item.showCopy">
              <n-button text>{{ $t('ms.description.copySuccess') }}</n-button>
              <n-button text>{{ $t('ms.description.copy') }}</n-button>
            </template>
          </template>
        </slot>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ms-description {
  @apply flex max-h-full flex-wrap overflow-auto;

  .ms-description-item {
    @apply flex;

    width: calc(100% / v-bind(column));
  }

  .ms-description-item-label {
    @apply font-normal;

    padding-right: 16px;

    word-wrap: break-word;
  }

  .ms-description-item-value,
  .ms-description-item-value--tagline {
    @apply relative flex-1 overflow-hidden break-all align-top;
  }

  .ms-description-item-value {
    /* stylelint-disable-next-line value-no-vendor-prefix */
    display: -webkit-box;
    text-overflow: ellipsis;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
  }

  .ms-description-item-value--one-line {
    -webkit-line-clamp: 1;
  }
}
</style>