<script setup lang="ts">
import {useI18n} from "vue-i18n";
import {useAppStore} from "/@/store";
import useTemplateStore from "/@/store/modules/setting/template.ts";
import {computed, ref, watch} from "vue";
import {hasAnyPermission} from "/@/utils/permission.ts";

const {t} = useI18n();
const appStore = useAppStore();
const templateStore = useTemplateStore();

const currentOrgId = computed(() => appStore.currentOrgId);

const props = defineProps<{
  cardItem: Record<string, any>;
  mode: 'organization' | 'project';
}>();
const emit = defineEmits<{
  (e: 'fieldSetting', key: string): void;
  (e: 'templateManagement', key: string): void;
  (e: 'workflowSetup', key: string): void;
  (e: 'updateState'): void;
}>();
const fieldSetting = () => {
  emit('fieldSetting', props.cardItem.key);
};
const templateManagement = () => {
  emit('templateManagement', props.cardItem.key);
};
const isEnableProject = computed(() => {
  return templateStore.projectStatus[props.cardItem.key];
});
const hasEnablePermission = computed(() => hasAnyPermission(['ORGANIZATION_TEMPLATE:READ+ENABLE']));
const isShow = computed(() => {
  if (props.cardItem.key === 'BUG') {
    return true;
  }
  return !hasEnablePermission.value ? false : !isEnableProject.value;
});
const templateCardInfo = ref<Record<string, any>>({});
watch(
    () => props.cardItem,
    (val) => {
      if (val) {
        templateCardInfo.value = {...props.cardItem};
      }
    },
    {deep: true}
);
</script>

<template>
  <div class="outerWrapper p-[3px]">
    <div class="innerWrapper">
      <div class="content">
<!--        <div class="logo-img h-[48px] w-[48px]">-->
<!--          <svg-icon width="36px" height="36px" :name="props.cardItem.value"></svg-icon>-->
<!--        </div>-->
        <div class="template-operation">
          <div class="flex items-center">
            <span class="font-medium">{{ props.cardItem.name }}</span>
            <span v-if="isEnableProject" class="enable">{{ t('system.orgTemplate.enabledTemplates') }}</span>
          </div>
          <div class="flex min-w-[300px] flex-nowrap items-center">
            <!-- 字段设置 -->
            <span class="operation hover:text-[rgb(var(--primary-5))]">
              <span @click="fieldSetting">{{ t('system.orgTemplate.fieldSetting') }}</span>
              <n-divider vertical/>
            </span>
            <!-- 模板列表 -->
            <span class="operation hover:text-[rgb(var(--primary-5))]">
              <span @click="templateManagement">{{ t('system.orgTemplate.TemplateManagementList') }}</span>
              <n-divider v-if="isShow" vertical/>
            </span>
            <!-- 工作流 -->
            <!--            <span v-if="props.cardItem.key === 'BUG'" class="operation hover:text-[rgb(var(&#45;&#45;primary-5))]">-->
            <!--              <span @click="workflowSetup">{{ t('system.orgTemplate.workflowSetup') }}</span>-->
            <!--              <n-divider-->
            <!--                  v-if="-->
            <!--                  hasEnablePermission &&-->
            <!--                  props.mode === 'organization' &&-->
            <!--                  !isEnableProject &&-->
            <!--                  props.cardItem.key === 'BUG'-->
            <!--                "-->
            <!--                  v-permission="['ORGANIZATION_TEMPLATE:READ+ENABLE']"-->
            <!--                  direction="vertical"-->
            <!--              />-->
            <!--            </span>-->
            <!-- 启用项目模板 只有组织可以启用 -->
            <span
                v-if="hasEnablePermission && props.mode === 'organization' && !isEnableProject"
                class="rounded p-[2px] hover:bg-[rgb(var(--primary-9))]"
            >
<!--              <MsTableMoreAction :list="moreActions" @select="handleMoreActionSelect"/>-->
              xxxxx
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.n-divider--vertical) {
  margin: 0 8px;
}
.outerWrapper {
  box-shadow: 0 6px 15px rgba(120 56 135/ 5%);
  @apply rounded bg-white;
  .innerWrapper {
    @apply rounded p-6;
    .content {
      @apply flex;
      .logo-img {

        @apply mr-3 flex items-center justify-center bg-white;
      }
      .template-operation {
        .operation {
          cursor: pointer;
        }
        .enable {
          height: 20px;
          font-size: 12px;
          line-height: 14px;
          @apply ml-4 rounded p-1;
        }
        @apply flex flex-col justify-between;
      }
    }
  }
}
</style>