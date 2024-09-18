<script setup lang="ts">
import {NUpload, NUploadDragger} from 'naive-ui';
import {computed} from "vue";

interface Props {
  width?: number;
  height?: number;
  max?: number;
  multiple?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  width: 104,
  height: 104,
  multiple: false,
  max: 1
})
const getCSSProperties = computed(() => {
  return {
    width: `${props.width}px`,
    height: `${props.height}px`,
  };
});
</script>

<template>
  <div :style="getCSSProperties">
    <n-upload class="w-auto"
        v-bind="$props"
        directory-dnd
        action="https://www.mocky.io/v2/5e4bafc63100007100d8b70f"
    >
      <n-upload-dragger>
        <div style="margin-bottom: 12px">
          <n-icon size="48" :depth="3">
            <ArchiveIcon/>
          </n-icon>
        </div>
        <n-text style="font-size: 16px">
          点击或者拖动文件到该区域来上传
        </n-text>
        <n-p depth="3" style="margin: 8px 0 0 0">
          请不要上传敏感数据，比如你的银行卡号和密码，信用卡号有效期和安全码
        </n-p>
      </n-upload-dragger>
    </n-upload>
  </div>

</template>

<style scoped>

</style>