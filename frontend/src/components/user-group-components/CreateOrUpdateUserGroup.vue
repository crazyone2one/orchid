<script setup lang="ts">
import {AuthScopeEnum} from "/@/enums/common-enum.ts";
import {UserGroupItem} from "/@/models/setting/user-group.ts";
import {inject, reactive, ref, watchEffect} from "vue";
import type {FormInst} from "naive-ui";
import {updateOrAddOrgUserGroup, updateOrAddUserGroup} from "/@/api/modules/setting/user-group.ts";
import {useAppStore} from "/@/store";
import {updateOrAddProjectUserGroup} from "/@/api/modules/project-management/user-group.ts";
import {useI18n} from "vue-i18n";

const props = defineProps<{
  id?: string;
  list: UserGroupItem[];
  visible: boolean;
  defaultName?: string;
  // 权限范围
  authScope: AuthScopeEnum;
}>();
const emit = defineEmits<{
  (e: 'cancel', value: boolean): void;
  (e: 'submit', currentId: string): void;
}>();
const systemType = inject<AuthScopeEnum>('systemType');
const appStore = useAppStore()
const {t} = useI18n()
const currentVisible = ref(props.visible);
const formRef = ref<FormInst | null>(null);
const handleOutsideClick = () => {
  if (currentVisible.value) {
    handleCancel();
  }
}
const handleCancel = () => {
  emit('cancel', false);
};
const form = reactive({
  name: '',
});

const loading = ref(false);
const handleSubmit = (e: MouseEvent) => {
  e.preventDefault();
  formRef.value?.validate(async (errors) => {
    if (errors) {
      return
    }
    try {
      loading.value = true;
      let res: UserGroupItem | undefined;
      if (systemType === AuthScopeEnum.SYSTEM) {
        res = await updateOrAddUserGroup({id: props.id, name: form.name, type: props.authScope});
      } else if (systemType === AuthScopeEnum.ORGANIZATION) {
        // 组织用户组
        res = await updateOrAddOrgUserGroup({
          id: props.id,
          name: form.name,
          type: props.authScope,
          scopeId: appStore.currentOrgId,
        });
      } else {
        // 项目用户组 项目用户组只有创建
        res = await updateOrAddProjectUserGroup({name: form.name});
      }
      if (res) {
        window.$message.success(
            props.id ? t('system.userGroup.updateUserGroupSuccess') : t('system.userGroup.addUserGroupSuccess')
        );
        emit('submit', res.id);
        handleCancel();
      }
    } catch (e) {
      console.log(e)
    } finally {
      loading.value = false;
    }
  })
};
watchEffect(() => {
  currentVisible.value = props.visible;
  form.name = props.defaultName || '';
});
</script>

<template>
  <n-popover :show="currentVisible" trigger="click" placement="right-end" class="w-[350px]"
             :content-class="props.id ? 'move-left' : ''">
    <template #trigger>
      <slot></slot>
    </template>
    <div v-outer="handleOutsideClick">
      <div>
        <n-form ref="formRef" :model="form">
          <div class="mb-[8px] text-[14px] font-medium">
            {{ props.id ? $t('system.userGroup.rename') : $t('system.userGroup.createUserGroup') }}
          </div>
          <n-form-item path="name">
            <n-input v-model:value="form.name" size="small" clearable :placeholder="$t('system.userGroup.searchHolder')"
                     :maxlength="255"/>
            <span v-if="!props.id" class="mt-[8px] text-[13px] font-medium">
                {{ $t('system.userGroup.createUserGroupTip') }}
              </span>
          </n-form-item>
        </n-form>
      </div>
      <div class="flex flex-row flex-nowrap justify-end gap-2">
        <n-button secondary size="tiny" :disabled="loading">{{ $t('common.cancel') }}</n-button>
        <n-button type="primary" size="tiny" :loading="loading" @click="handleSubmit">
          {{ props.id ? $t('common.confirm') : $t('common.create') }}
        </n-button>
      </div>
    </div>
  </n-popover>
</template>

<style scoped>
.move-left {
  position: relative;
  right: 22px;
}
</style>