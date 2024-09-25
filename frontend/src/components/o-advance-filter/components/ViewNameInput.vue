<script setup lang="ts">
import type {FormInst, FormItemRule, InputInst} from "naive-ui";
import {ref} from "vue";
import {useI18n} from "vue-i18n";

const props = defineProps<{
  allNames: string[];
  notShowWordLimit?: boolean;
}>();
const form = defineModel<Record<string, any>>('form', {
  required: true,
});

const emit = defineEmits<{
  (e: 'handleSubmit'): void;
}>();
const {t} = useI18n()
const formRef = ref<FormInst | null>(null)
const inputRef = ref<InputInst>();
const rules = {
  name: [{
    required: true, message: t('advanceFilter.viewNameRequired'), trigger: ['change', 'input'],
    validator(_rule: FormItemRule, value: string) {
      if (props.allNames.includes(value)) {
        return new Error(t('advanceFilter.viewNameNotRepeat'))
      }
      return true
    },
  }]
}
const handleSubmit = () => {
  formRef.value?.validate(errors => {
    if (!errors) {
      emit('handleSubmit');
    }
  })
}
const inputFocus = () => {
  inputRef.value?.focus()
}
defineExpose({
  inputFocus,
  // validateForm,
});
</script>

<template>
  <n-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-placement="top"
  >
    <n-form-item path="name">
      <n-input ref="inputRef" v-model:value="form.name" :placeholder="t('advanceFilter.viewNamePlaceholder')"
               :maxlength="255"
               :show-count="!props.notShowWordLimit" @keyup="handleSubmit" @blur="handleSubmit"/>
    </n-form-item>
  </n-form>
</template>

<style scoped>

</style>