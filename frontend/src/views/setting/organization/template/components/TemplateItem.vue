<script setup lang="ts">
import {useI18n} from "vue-i18n";
import {useAppStore} from "/@/store";
import useTemplateStore from "/@/store/modules/setting/template.ts";
import {computed, ref, watch} from "vue";
import {hasAnyPermission} from "/@/utils/permission.ts";
import type {DropdownOption} from "naive-ui";
import TableMoreAction from '/@/components/o-table-more-action/index.vue'
import {useRequest} from "alova/client";
import {enableOrOffTemplate} from "/@/api/modules/setting/template.ts";

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
const showEnableVisible = ref<boolean>(false);
const validateKeyWord = ref<string>('');
const fieldSetting = () => {
  emit('fieldSetting', props.cardItem.key);
};
const templateManagement = () => {
  emit('templateManagement', props.cardItem.key);
};
const orgName = computed(() => {
  // if (licenseStore.hasLicense()) {
  //   return appStore.ordList.find((item: any) => item.id === appStore.currentOrgId)?.name;
  // }
  return '默认组织';
});
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
const moreActions = ref<DropdownOption[]>([
  {
    label: t('system.orgTemplate.enable'),
    key: 'enable',
    danger: true,
    permission: ['ORGANIZATION_TEMPLATE:READ+ENABLE'],
  },
]);
const enableHandler = () => {
  showEnableVisible.value = true;
}
const cancelHandler = () => {
  showEnableVisible.value = false;
  validateKeyWord.value = '';
}
const {
  loading,
  send
} = useRequest((() => enableOrOffTemplate(currentOrgId.value, props.cardItem.key)), {immediate: false})
const handleOk = () => {
  if (validateKeyWord.value.trim() !== '' && validateKeyWord.value !== orgName.value) {
    window.$message.success(t('system.orgTemplate.orgNameTip'));
    return false;
  }
  if (props.mode) {
    send().then(async () => {
      window.$message.success(t('system.orgTemplate.enabledSuccessfully'));
      await templateStore.getStatus();
      emit('updateState');
      showEnableVisible.value = false;
    })
  }
}
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
            <span v-if="hasEnablePermission && props.mode === 'organization' && !isEnableProject"
                  class="rounded p-[2px] hover:bg-[rgb(var(--primary-9))]">
              <table-more-action :list="moreActions" @select="enableHandler"/>
            </span>
          </div>
        </div>
      </div>
    </div>
    <n-modal v-model:show="showEnableVisible" preset="dialog"
             :mask-closable="false" @close="cancelHandler">
      <template #header>
        <div>{{ t('system.orgTemplate.enableTip') }}</div>
      </template>
      <div>{{ t('system.orgTemplate.enableWarningTip') }}</div>
      <n-input v-model:value="validateKeyWord" :placeholder="t('system.orgTemplate.searchOrgPlaceholder')" clearable
               class="mb-4 mt-[8px]" :maxlength="255"/>
      <template #action>
        <div>
          <n-button secondary @click="cancelHandler">{{ t('common.cancel') }}</n-button>
          <n-button class="ml-3" type="error" :loading="loading"
                    :disabled="!validateKeyWord.trim().length" @click="handleOk">
            {{ t('common.confirmEnable') }}
          </n-button>
        </div>
      </template>
    </n-modal>
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