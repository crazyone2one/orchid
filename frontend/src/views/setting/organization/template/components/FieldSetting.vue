<script setup lang="ts">
import {computed, ref} from "vue";
import {useAppStore} from "/@/store";
import {useRoute} from "vue-router";
import {useI18n} from "vue-i18n";
import useTemplateStore from "/@/store/modules/setting/template.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";
import {SceneType} from "/@/models/setting/template.ts";
import {NAlert} from "naive-ui";


const props = defineProps<{
  mode: 'organization' | 'project';
  deletePermission: string[];
  createPermission: string[];
  updatePermission: string[];
}>();
const appStore = useAppStore();
const templateStore = useTemplateStore();
const route = useRoute();
const {t} = useI18n()
const currentOrd = computed(() => appStore.currentOrgId);
const currentProjectId = computed(() => appStore.currentProjectId);
const templateType = computed(() => {
  switch (route.query.type) {
    case 'API':
      return t('system.orgTemplate.templateApi');
    case 'BUG':
      return t('system.orgTemplate.templateBug');
    default:
      return t('system.orgTemplate.templateCase');
  }
});
// 是否开启模板(项目/组织)
const isEnableTemplate = computed(() => {
  if (props.mode === 'organization') {
    return templateStore.ordStatus[route.query.type as string];
  }
  return templateStore.projectStatus[route.query.type as string];
});

const hasOperationPermission = computed(() =>
    hasAnyPermission([...props.updatePermission, ...props.deletePermission])
);
const isEnabledTemplate = computed(() => {
  return props.mode === 'organization'
      ? templateStore.projectStatus[scene.value as string]
      : !templateStore.projectStatus[scene.value as string];
});
const isShowTip = ref<boolean>(true);
const scene = ref<SceneType>(route.query.type);
const getParams = () => {
  scene.value = route.query.type;
  return {
    scene: scene.value,
    scopedId: props.mode === 'organization' ? currentOrd.value : currentProjectId.value,
  };
};
const noRemindHandler = () => {
  isShowTip.value = false;
  // addVisited();
};
</script>

<template>
  <div>
    <n-alert v-if="isShowTip && hasAnyPermission([...props.createPermission, ...props.updatePermission])"
             class="mb-6"
             :type="isEnabledTemplate && props.mode === 'organization' ? 'warning' : 'info'">
      <div class="flex items-start justify-between">
        <span>
          {{
            isEnabledTemplate && props.mode === 'organization'
                ? t('system.orgTemplate.enableDescription')
                : t('system.orgTemplate.fieldLimit')
          }}</span
        >
        <span class="cursor-pointer" @click="noRemindHandler">
        {{ t('system.orgTemplate.noReminders') }}
      </span>
      </div>
    </n-alert>
    <div class="mb-4 flex items-center justify-between">
      <span v-if="isEnabledTemplate" class="font-medium">{{ t('system.orgTemplate.fieldList') }}</span>
      <n-button v-if="!isEnabledTemplate && hasAnyPermission(props.createPermission)"
                type="primary">
        {{ t('system.orgTemplate.addField') }}
      </n-button>
      <n-input :placeholder="t('system.orgTemplate.searchTip')" clearable class="w-[230px]"/>
    </div>
    <n-data-table :data="[]"/>
  </div>
</template>

<style scoped>

</style>