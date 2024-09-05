<script setup lang="ts">

import {useI18n} from "vue-i18n";
import {ref, watchEffect} from "vue";
import type {FormInst} from "naive-ui";
import UserSelect from '/@/components/o-user-selector/index.vue'
import {UserRequestTypeEnum} from "/@/components/o-user-selector/utils.ts";
import {useForm} from "alova/client";
import {addUserToUserGroup} from "/@/api/modules/project-management/user-group.ts";

const {t} = useI18n();
const props = defineProps<{
  visible: boolean;
  projectId: string;
  userRoleId: string;
}>();

const emit = defineEmits<{
  (e: 'cancel', shouldSearch: boolean): void;
}>();
const currentVisible = ref(props.visible);
const formRef = ref<FormInst | null>(null)
const handleCancel = (shouldSearch: boolean) => {
  resetForm()
  emit('cancel', shouldSearch);
};
const {
  // 提交状态
  loading,
  // 响应式的表单数据，内容由initialForm决定
  form, send: submit, reset: resetForm
} = useForm(
    formData => {
      const {projectId, userRoleId} = props;
      return addUserToUserGroup({userIds: formData?.name, projectId, userRoleId});
    },
    {
      // 初始化表单数据
      initialForm: {
        name: [],
      },
    }
);
const handleAddMember = () => {
  console.log(form.value)
  formRef.value?.validate(errors => {
    if (errors) {
      return
    }
    submit().then(() => {
      window.$message.success(t('system.organization.addSuccess'));
      handleCancel(true);
    })
  })
}
watchEffect(() => {
  currentVisible.value = props.visible;
});

</script>

<template>
  <n-modal v-model:show="currentVisible" preset="dialog" :mask-closable="false" @close="handleCancel(false)">
    <template #header>
      {{ t('system.organization.addMember') }}
    </template>
    <div>
      <n-form ref="formRef" :model="form">
        <n-form-item :label="$t('system.organization.member')" path="name"
                     :rule="{ required: true, message: t('system.organization.addMemberRequired') }">
          <user-select v-model="form.name" :type="UserRequestTypeEnum.PROJECT_USER_GROUP" mode="remote"
                       :load-option-params="{ projectId: props.projectId, userRoleId: props.userRoleId }"
                       disabled-key="checkRoleFlag"/>
        </n-form-item>
      </n-form>
    </div>
    <template #action>
      <n-button secondary :loading="loading" @click="handleCancel(false)">
        {{ t('common.cancel') }}
      </n-button>
      <n-button type="primary" :loading="loading" @click="handleAddMember">
        {{ t('common.add') }}
      </n-button>
    </template>
  </n-modal>
</template>

<style scoped>

</style>