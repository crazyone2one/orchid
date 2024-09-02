<script setup lang="ts">
import ProviderView from "./layout/components/ProviderView.vue";
import {darkTheme, useOsTheme} from "naive-ui";
import {computed, onBeforeMount} from "vue";
import {useEventListener, useWindowSize} from "@vueuse/core";
import {useAppStore} from "/@/store";

const appStore = useAppStore()
const osTheme = useOsTheme();
const theme = computed(() => (osTheme.value === "dark" ? darkTheme : null));

onBeforeMount(() => {
  const {height} = useWindowSize();
  appStore.innerHeight = height.value;
  appStore.getProjectInfos();
})
/** 屏幕大小改变时重新赋值innerHeight */
useEventListener(window, 'resize', () => {
  const {height} = useWindowSize();
  appStore.innerHeight = height.value;
});
</script>

<template>
  <n-config-provider :theme="theme">
    <n-global-style/>
    <n-loading-bar-provider>
      <n-message-provider>
        <n-dialog-provider>
          <n-notification-provider>
            <n-modal-provider>
              <slot/>
              <provider-view/>
            </n-modal-provider>
          </n-notification-provider>
        </n-dialog-provider>
      </n-message-provider>
    </n-loading-bar-provider>
  </n-config-provider>
</template>

<style scoped>
.logo {
  height: 6em;
  padding: 1.5em;
  will-change: filter;
  transition: filter 300ms;
}

.logo:hover {
  filter: drop-shadow(0 0 2em #646cffaa);
}

.logo.vue:hover {
  filter: drop-shadow(0 0 2em #42b883aa);
}
</style>
