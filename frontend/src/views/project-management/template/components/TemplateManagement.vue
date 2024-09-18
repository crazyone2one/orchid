<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue';
import {useRoute} from "vue-router";
import {useI18n} from "vue-i18n";
import {useAppStore} from "/@/store";
import {computed} from "vue";
import {hasAnyPermission} from "/@/utils/permission.ts";
import useTemplateStore from "/@/store/modules/setting/template.ts";
import {getTemplateName} from "../../../setting/organization/template/components/field-setting.ts";

const route = useRoute();
const {t} = useI18n();
const appStore = useAppStore();
const templateStore = useTemplateStore();
const isShowList = computed(() => {
  if (!hasAnyPermission(['PROJECT_TEMPLATE:READ+ADD'])) {
    return true;
  }
  if (isEnableOrdTemplate.value && route.query.type === 'BUG') {
    return true;
  }
  return route.query.type !== 'BUG';
});
const scene = route.query.type;
const isEnableOrdTemplate = computed(() => {
  return !templateStore.projectStatus[scene as string];
});
</script>

<template>
  <o-card simple has-breadcrumb>
    <div class="mb-4 flex items-center justify-between">
    <span v-if="isShowList" class="font-medium">
      {{ t('system.orgTemplate.templateList', {type: getTemplateName('project', route.query.type as string)}) }}
    </span>
      <n-button v-if="!isEnableOrdTemplate && route.query.type === 'BUG'"
                v-permission="['PROJECT_TEMPLATE:READ+ADD']"
                type="primary">
        {{t('system.orgTemplate.createTemplateType', {type: getTemplateName('project', route.query.type as string),})}}
      </n-button>
      <div>
        <n-input :placeholder="t('system.orgTemplate.searchTip')" clearable/>
      </div>
    </div>
    <n-data-table/>
  </o-card>
</template>

<style scoped>

</style>