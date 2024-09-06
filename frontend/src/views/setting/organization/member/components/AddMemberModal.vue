<script setup lang="ts">

import {useI18n} from "vue-i18n";
import {computed, ref, watch, watchEffect} from "vue";
import type {FormInst} from "naive-ui";
import UserSelect from '/@/components/o-user-selector/index.vue'
import {UserRequestTypeEnum} from "/@/components/o-user-selector/utils.ts";
import {useForm} from "alova/client";
import {useAppStore} from "/@/store";
import {LinkList, MemberItem} from "/@/models/setting/member.ts";
import {cloneDeep} from "lodash-es";
import {addOrUpdate} from "/@/api/modules/setting/member.ts";

export interface InitFromType {
  organizationId?: string;
  userRoleIds: string[];
  memberIds: (string | number)[];
  projectIds: (string | number)[];
}

const {t} = useI18n();
const appStore = useAppStore();
const lastOrganizationId = computed(() => appStore.currentOrgId);
const props = defineProps<{
  visible: boolean;
  userGroupOptions: LinkList;
}>();
const emits = defineEmits<{
  (event: 'update:visible', visible: boolean): void;
  (event: 'success'): void;
}>();
const currentVisible = ref<boolean>(false);
const title = ref<string>('');
const type = ref<string>('');
const memberName = ref<string>('');
const initFormValue: InitFromType = {
  organizationId: lastOrganizationId.value,
  userRoleIds: ['org_member'],
  memberIds: [],
  projectIds: [],
};
const formRef = ref<FormInst | null>(null)
const handleCancel = () => {
  resetForm()
  formRef.value?.restoreValidation()
  currentVisible.value = false;
};
const edit = (record: MemberItem) => {
  const {userRoleIdNameMap, projectIdNameMap} = record;
  form.value.memberIds = [record.id as string];
  form.value.userRoleIds = (userRoleIdNameMap || []).map((item) => item.id);
  form.value.projectIds = (projectIdNameMap || []).map((item) => item.id);
  memberName.value = record.name;
};
const {
  // 提交状态
  loading,
  // 响应式的表单数据，内容由initialForm决定
  form, send: submit, reset: resetForm
} = useForm(
    formData => {
      let params = {};
      const {organizationId, memberIds, userRoleIds, projectIds} = formData;
      params = Object.assign(params, {
        organizationId,
        userRoleIds,
      });
      params = type.value === 'add' ?
          {
            ...params,
            memberIds,
          }
          :
          {
            ...params,
            projectIds,
            memberId: memberIds?.join(),
          };
      return addOrUpdate(params, type.value);
    },
    {
      // 初始化表单数据
      initialForm: cloneDeep(initFormValue),
    }
);
const handleOK = () => {
  formRef.value?.validate(errors => {
    if (errors) {
      return
    }
    submit().then(() => {
      window.$message.success(
          type.value === 'add'
              ? t('organization.member.batchModalSuccess')
              : t('organization.member.batchUpdateSuccess')
      );
      handleCancel();
      emits('success');
    })
  })
}
watchEffect(() => {
  currentVisible.value = props.visible;
});
watch(
    () => currentVisible.value,
    (val) => {
      emits('update:visible', val);
    }
);
defineExpose({
  title,
  type,
  edit,
});
</script>

<template>
  <n-modal v-model:show="currentVisible" preset="dialog" :mask-closable="false" @close="handleCancel">
    <template #header>
      {{
        type === 'add'
            ? t('organization.member.addMember')
            : t('organization.member.updateMember', {name: memberName})
      }}
    </template>
    <div>
      <n-form ref="formRef" :model="form">
        <n-form-item v-if="type === 'edit'" :label="$t('organization.member.project')" path="name">
          <user-select v-model="form.projectIds" :type="UserRequestTypeEnum.SYSTEM_ORGANIZATION_PROJECT" mode="remote"
                       :load-option-params="{organizationId: lastOrganizationId}"
                       placeholder="organization.member.selectProjectScope"/>
        </n-form-item>
        <n-form-item v-else :label="$t('organization.member.member')" path="memberIds"
                     :rule="{ required: true, message: t('organization.member.selectMemberEmptyTip') }">
          <user-select v-model="form.memberIds" :type="UserRequestTypeEnum.SYSTEM_ORGANIZATION_MEMBER" mode="remote"
                       :load-option-params="{organizationId: lastOrganizationId}"
                       placeholder="organization.member.selectMemberScope" disabled-key="disabled"/>
        </n-form-item>
        <n-form-item :label="$t('organization.member.tableColumnUserGroup')" path="userRoleIds"
                     :rule="{ required: true, message: t('organization.member.selectUserEmptyTip') }">
          <n-select v-model:value="form.userRoleIds" :options="props.userGroupOptions" multiple filterable
                    :placeholder="t('organization.member.selectUserScope')" label-field="name" value-field="id"/>
        </n-form-item>
      </n-form>
    </div>
    <template #action>
      <n-button secondary :loading="loading" @click="handleCancel">
        {{ t('organization.member.Cancel') }}
      </n-button>
      <n-button type="primary" :loading="loading" @click="handleOK">
        {{ t('organization.member.Confirm') }}
      </n-button>
    </template>
  </n-modal>
</template>

<style scoped>

</style>