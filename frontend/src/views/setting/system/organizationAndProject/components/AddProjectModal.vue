<script setup lang="ts">
import {computed, ref, watchEffect} from "vue";
import type {FormInst} from "naive-ui";
import UserSelector from '/@/components/o-user-selector/index.vue'

import {CreateOrUpdateSystemProjectParams, OrgProjectTableItem, SystemOrgOption} from "/@/models/orgAndProject.ts";
import {useI18n} from "vue-i18n";
import {UserRequestTypeEnum} from "/@/components/o-user-selector/utils.ts";
import {createOrUpdateProject, getSystemOrgOption} from "/@/api/modules/setting/system-org-project.ts";
import {useForm} from "alova/client";
import {useAppStore, useUserStore} from "/@/store";
import {showUpdateOrCreateMessage} from "/@/views/setting/utils.ts";
import {cloneDeep} from "lodash-es";

const showModal = defineModel<boolean>('visible', {
  default: false,
});
const props = defineProps<{
  currentProject?: CreateOrUpdateSystemProjectParams;
}>();
const userStore = useUserStore()
const appStore = useAppStore();
const currentOrgId = computed(() => appStore.currentOrgId);
const isEdit = computed(() => !!props.currentProject?.id);
const emit = defineEmits<{
  (e: 'cancel', shouldSearch: boolean): void;
}>();
const {t} = useI18n()
const formRef = ref<FormInst | null>(null)
const moduleOption = [
  // { label: 'menu.workbench', value: 'workstation' },
  {label: 'menu.testPlan', value: 'testPlan'},
  {label: 'menu.bugManagement', value: 'bugManagement'},
  {label: 'menu.caseManagement', value: 'caseManagement'},
  {label: 'menu.apiTest', value: 'apiTest'},
  // { label: 'menu.uiTest', value: 'uiTest' },
  // { label: 'menu.performanceTest', value: 'loadTest' },
];
const allModuleIds = ['bugManagement', 'caseManagement', 'apiTest', 'testPlan'];
const affiliatedOrgOption = ref<SystemOrgOption[]>([]);
const rules =
    {
      name: [
        {required: true, message: t('system.project.projectNameRequired')},
        {maxLength: 255, message: t('common.nameIsTooLang')},
      ],
      organizationId: [{required: true, message: t('system.project.affiliatedOrgRequired')}],
      userIds: [{required: true, message: t('system.project.projectAdminIsNotNull')}]
    }

const handleCancel = (search: boolean) => {
  formRef.value?.restoreValidation()
  reset()
  emit('cancel', search);
};
const initAffiliatedOrgOption = async () => {
  affiliatedOrgOption.value = await getSystemOrgOption();
}
const {
  loading, form, send: submit, reset
}
    = useForm(
    formData => {
      const param: Partial<OrgProjectTableItem> = cloneDeep(formData) as Partial<OrgProjectTableItem>
      param.id = isEdit.value ? props.currentProject?.id : '';
      return createOrUpdateProject(param);
    },
    {
      // 初始化表单数据
      initialForm: {
        id: '',
        name: '',
        userIds: userStore.id ? [userStore.id] : [],
        organizationId: '',
        description: '',
        enable: true,
        moduleIds: allModuleIds,
        resourcePoolIds: [],
      },
      resetAfterSubmiting: true
    }
);
const handleSubmit = (e: MouseEvent) => {
  e.preventDefault()
  formRef.value?.validate(errors => {
    if (errors) {
      return
    }
    submit().then((res) => {
      showUpdateOrCreateMessage(isEdit.value, res.id, res.organizationId);
      handleCancel(true);
      appStore.initProjectList();
    })
  })
}
watchEffect(() => {
  initAffiliatedOrgOption();
  if (props.currentProject?.id) {
    if (props.currentProject) {
      form.value.id = props.currentProject.id;
      form.value.name = props.currentProject.name;
      form.value.description = props.currentProject.description;
      form.value.enable = props.currentProject.enable;
      form.value.userIds = props.currentProject.userIds;
      form.value.organizationId = props.currentProject.organizationId;
      form.value.moduleIds = props.currentProject.moduleIds;
      form.value.resourcePoolIds = props.currentProject.resourcePoolIds;
    }
  }
})
</script>

<template>
  <n-modal
      v-model:show="showModal"
      preset="dialog"
      :mask-closable="false"
      @close="handleCancel(false)"
      @negative-click="handleCancel(false)">
    <template #header>
      <span v-if="isEdit">
        {{ $t('system.project.updateProject') }}
        <span>({{ props.currentProject?.name }})</span>
      </span>
      <span v-else>
        {{ $t('system.project.createProject') }}
      </span>
    </template>
    <div class="form">
      <n-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-placement="left"
          label-width="auto"
          require-mark-placement="right-hanging"
      >
        <n-form-item :label="$t('system.project.name')" path="name">
          <n-input v-model:value="form.name" :placeholder="$t('system.project.projectNamePlaceholder')"/>
        </n-form-item>
        <n-form-item :label="$t('system.project.affiliatedOrg')" path="organizationId">
          <n-select v-model:value="form.organizationId" :options="affiliatedOrgOption"
                    :placeholder="t('system.project.affiliatedOrgPlaceholder')" filterable
                    label-field="name" value-field="id"/>
        </n-form-item>
        <n-form-item :label="$t('system.project.projectAdmin')" path="userIds">
          <user-selector v-model="form.userIds" mode="remote" :type="UserRequestTypeEnum.SYSTEM_PROJECT_ADMIN"
                         placeholder="system.project.pleaseSelectAdmin"
                         :load-option-params="{ organizationId: currentOrgId,}"/>
        </n-form-item>
        <n-form-item :label="$t('system.project.moduleSetting')" path="module">
          <n-checkbox-group v-model:value="form.moduleIds">
            <n-space item-style="display: flex;" align="center">
              <n-checkbox v-for="item in moduleOption" :key="item.value" :value="item.value" :label="t(item.label)"/>
            </n-space>
          </n-checkbox-group>
        </n-form-item>
        <n-form-item :label="$t('system.organization.status')" path="enable">
          <div class="flex flex-row items-center gap-[4px]">
            <n-switch v-model:value="form.enable"/>
            <n-tooltip>
              <template #trigger>
                <div class="i-ic-twotone-gpp-maybe text-orange-400"/>
              </template>
              {{ t('system.project.createTip') }}
            </n-tooltip>
          </div>
        </n-form-item>
        <n-form-item :label="$t('common.desc')" path="description">
          <n-input v-model:value="form.description"
                   type="textarea"
                   :placeholder="$t('system.project.descriptionPlaceholder')" clearable/>
        </n-form-item>
      </n-form>
    </div>
    <template #action>
      <n-button secondary :disabled="loading" @click="handleCancel(false)"> {{ $t('common.cancel') }}</n-button>
      <n-button type="primary" :loading="loading" @click="handleSubmit">
        {{ isEdit ? $t('common.update') : $t('common.create') }}
      </n-button>

    </template>
  </n-modal>
</template>

<style scoped>

</style>