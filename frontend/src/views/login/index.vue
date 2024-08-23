<script setup lang="ts">
import {FormInst, NButton, NForm, NFormItem, NInput} from 'naive-ui'
import {computed, ref} from "vue";
import {LoginData} from "/@/models/user.ts";
import useUserStore from "/@/store/modules/user";
import {useRouter} from "vue-router";
import {useAppStore} from "/@/store";
import {getFirstRouteNameByPermission, routerNameHasPermission} from "/@/utils/permission.ts";
import {useI18n} from "vue-i18n";

const {t} = useI18n();
const router = useRouter();
const userStore = useUserStore()
const appStore = useAppStore();
const formRef = ref<FormInst | null>(null)
const model = ref<LoginData>({
  username: '', password: ''
})
const disabled = computed<boolean>(() => model.value.username === '' || model.value.password === '');
const loading = ref(false)
const rules = {
  username: [
    {
      required: true,
      message: t('login.form.userName.errMsg'),
      trigger: 'blur'
    }
  ],
  password: [
    {
      required: true,
      message: t('login.form.password.errMsg'),
      trigger: 'blur'
    }
  ]
}
const handleLogin = (e: MouseEvent) => {
  window.$message.info('login')
  e.preventDefault()
  loading.value = true
  formRef.value?.validate((errors) => {
    if (!errors) {
      userStore.login(model.value).then(() => {
        window.$message.success('登录成功')
        const {redirect, ...othersQuery} = router.currentRoute.value.query;
        const redirectHasPermission =
            redirect &&
            routerNameHasPermission(redirect as string, router.getRoutes())
        const currentRouteName = getFirstRouteNameByPermission(router.getRoutes());
        router.push({
          name: redirectHasPermission ? (redirect as string) : currentRouteName,
          query: {
            ...othersQuery,
            orgId: appStore.currentOrgId,
            pId: appStore.currentProjectId,
          },
        });
      })
    }
  })
  loading.value = false
}
</script>

<template>
  <n-h1 style="--font-size: 60px; --font-weight: 100">ORCHID</n-h1>
  <n-card size="large" style="--padding-bottom: 30px">
    <n-h2 style="--font-weight: 400">{{ $t('login.form.accountLogin') }}</n-h2>
    <n-form
        ref="formRef"
        :model="model"
        :rules="rules"
        label-placement="left"
        label-width="auto"
        require-mark-placement="right-hanging"
    >
      <n-form-item path="username">
        <n-input v-model:value="model.username" :placeholder="$t('login.form.userName.placeholder')"/>
      </n-form-item>
      <n-form-item path="password">
        <n-input v-model:value="model.password" type="password" :maxlength="64" clearable
                 :placeholder="$t('login.form.password.placeholder')"/>
      </n-form-item>
    </n-form>
    <n-button type="primary" size="large" block :loading="loading" :disabled="disabled" @click="handleLogin">
      {{ $t('login.form.login') }}
    </n-button>
    <br/>
  </n-card>

</template>

<style scoped>
.n-h1 {
  margin: 20vh auto 20px;
  text-align: center;
  letter-spacing: 5px;
  opacity: 0.8;
}

.n-card {
  margin: 0 auto;
  max-width: 380px;
  box-shadow: var(--box-shadow);
}
</style>