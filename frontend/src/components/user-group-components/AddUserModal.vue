<script setup lang="ts">
import {useI18n} from "vue-i18n";
import {computed, inject, ref, watchEffect} from "vue";
import {AuthScopeEnum} from "/@/enums/common-enum.ts";
import {useAppStore} from "/@/store";
import {UserRequestTypeEnum} from "/@/components/o-user-selector/utils.ts";
import UserSelect from '/@/components/o-user-selector/index.vue'
import type {FormInst} from "naive-ui";
import {useForm} from "alova/client";
import {addOrgUserToUserGroup, addUserToUserGroup} from "/@/api/modules/setting/user-group.ts";

const {t} = useI18n();
const systemType = inject<AuthScopeEnum>('systemType');
const props = defineProps<{
  visible: boolean;
  currentId: string;
}>();

const appStore = useAppStore();
const currentOrgId = computed(() => appStore.currentOrgId);
const emit = defineEmits<{
  (e: 'cancel', shouldSearch: boolean): void;
}>();
const userSelectorProps = computed(() => {
  if (systemType === AuthScopeEnum.SYSTEM) {
    return {
      type: UserRequestTypeEnum.SYSTEM_USER_GROUP,
      loadOptionParams: {
        roleId: props.currentId,
      },
      disabledKey: 'exclude',
      mode: 'remote',
    };
  }
  return {
    type: UserRequestTypeEnum.ORGANIZATION_USER_GROUP,
    loadOptionParams: {
      roleId: props.currentId,
      organizationId: currentOrgId.value,
    },
    disabledKey: 'checkRoleFlag', mode: 'remote',
  };
});
const formRef = ref<FormInst | null>(null)
const showModal = ref(props.visible);

const {
  loading: submiting,
  form,
  send: submit,
} = useForm(
    formData => {
      if (systemType === AuthScopeEnum.SYSTEM) {
        return addUserToUserGroup({roleId: props.currentId, userIds: formData?.name as string[]});
      }
      if (systemType === AuthScopeEnum.ORGANIZATION) {
        return addOrgUserToUserGroup({
          userRoleId: props.currentId,
          userIds: formData?.name as string[],
          organizationId: currentOrgId.value,
        })
      }
    },
    {
      // 初始化表单数据
      initialForm: {
        name: [],
      }
    }
);
const handleCancel = (shouldSearch = false) => {
  form.value.name = [];
  emit('cancel', shouldSearch);
}
const handleBeforeOk = (e: MouseEvent) => {
  e.preventDefault();
  formRef.value?.validate(async (errors) => {
    if (errors) {
      return
    }
    await submit();
    handleCancel(true);
    window.$message.success(t('common.addSuccess'));
  })
};
watchEffect(() => {
  showModal.value = props.visible;
});
</script>

<template>
  <n-modal v-model:show="showModal" preset="dialog" title="Dialog"
           :mask-closable="false"
           @close="handleCancel(false)">
    <template #header>
      <div> {{ t('system.userGroup.addUser') }}</div>
    </template>
    <div>
      <n-form ref="formRef">
        <n-form-item :label="t('system.userGroup.user')">
          <user-select v-model="form.name" v-bind="userSelectorProps"/>
        </n-form-item>
      </n-form>
    </div>
    <template #action>
      <n-button secondary :loading="submiting" @click="handleCancel(false)">{{ t('common.cancel') }}</n-button>
      <n-button type="primary" :loading="submiting" :disabled="form.name.length === 0" @click="handleBeforeOk">
        {{ t('common.add') }}
      </n-button>
    </template>
  </n-modal>
</template>

<style scoped>

</style>