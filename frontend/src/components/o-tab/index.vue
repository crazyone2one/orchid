<script setup lang="ts">
import {ref} from "vue";

const props = withDefaults(
    defineProps<{
      mode?: 'origin' | 'button';
      contentTabList: { label: string | number; value: string | number }[];
      class?: string;
      getTextFunc?: (value: any) => string;
      noContent?: boolean;
      showBadge?: boolean;
      changeInterceptor?: (newVal: string | number, oldVal: string | number, done: () => void) => void;
      buttonSize?: 'small' | 'default';
    }>(),
    {
      mode: 'origin',
      showBadge: true,
      getTextFunc: (value: any) => value,
      class: '',
      buttonSize: 'default',
    }
);
const emit = defineEmits<{
  (e: 'change', value: string | number): void;
}>();
// 实际值，用于最终确认修改的 tab 值
const activeKey = defineModel<string | number>('activeKey', {
  default: '',
});
// 临时值，用于组件内部变更，但未影响到实际值
const tempActiveKey = ref(activeKey.value);
const handleTabClick = (value: string | number) => {
  if (value === activeKey.value) {
    return;
  }
  if (props.changeInterceptor) {
    // 存在拦截器，则先将临时值重置为实际值（此时实际值是未变更之前的值），再执行拦截器
    tempActiveKey.value = activeKey.value;
    props.changeInterceptor(value, activeKey.value, () => {
      // 拦截器成功回调=》修改实际值，也将临时值修改为实际值
      activeKey.value = value;
      tempActiveKey.value = value;
    });
  } else {
    // 不存在拦截器，直接修改实际值
    activeKey.value = value;
  }
  emit('change', value);
}
</script>

<template>
  <n-tabs v-if="props.mode === 'origin'" v-model:value="tempActiveKey" type="line" animated
          :class="[props.class, props.noContent ? 'no-content' : '']"
          @update:value="(value:string) =>handleTabClick(value)">
    <n-tab-pane v-for="item in props.contentTabList" :key="item.value" :name="item.value" :tab="item.label">
      <template v-if="props.showBadge" #tab>
        <n-badge v-if="props.getTextFunc(item.value) !== ''" :class="item.value === tempActiveKey ? 'active-badge' : ''"
                 :max="99" :value="props.getTextFunc(item.value)">
          <div class="mr-[4px]">
            {{ item.label }}
          </div>
        </n-badge>
        <div v-else>
          {{ item.label }}
        </div>
      </template>
    </n-tab-pane>
  </n-tabs>
  <div v-else>
    <div
        v-for="item of props.contentTabList"
        :key="item.value"
        class="ms-tab-button-item"
        :class="[
        item.value === tempActiveKey ? 'ms-tab-button-item--active' : '',
        props.buttonSize === 'small' ? 'ms-tab--button-item--small' : '',
      ]"
        @click="handleTabClick(item.value)"
    >
      {{ item.label }}
    </div>
  </div>
</template>

<style scoped>

</style>