<script setup lang="ts">
import {FormInst, NForm, NFormItem, NInput, NButton} from 'naive-ui'
import {ref} from "vue";
import {LoginData} from "/@/models/user.ts";
import useUserStore from "/@/store/modules/user";

const userStore = useUserStore()
const formRef = ref<FormInst | null>(null)
const model = ref<LoginData>({
  username: '', password: ''
})
const handleLogin = (e: MouseEvent) => {
  e.preventDefault()
  formRef.value?.validate((errors) => {
    if (!errors) {
      userStore.login(model.value).then(() => {
        console.log('login success')
      })
    }
  })
}
</script>

<template>
  <n-form
      ref="formRef"
      :model="model"
      label-placement="left"
      label-width="auto"
      require-mark-placement="right-hanging"
      :style="{
      maxWidth: '640px',
    }"
  >
    <n-form-item label="username" path="inputValue">
      <n-input v-model:value="model.username" placeholder="Input"/>
    </n-form-item>
    <n-form-item label="password" path="inputValue">
      <n-input v-model:value="model.password" placeholder="Input"/>
    </n-form-item>
    <div style="display: flex; justify-content: flex-end">
      <n-button round type="primary" @click="handleLogin">
        验证
      </n-button>
    </div>
  </n-form>
</template>

<style scoped>

</style>