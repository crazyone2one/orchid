<script setup lang="ts">
import {h} from "vue";
import {NAvatar, NText} from "naive-ui";
import {useI18n} from "vue-i18n";
import {useAppStore, useUserStore} from "/@/store";
import router from "/@/router";

const {t} = useI18n()
const userStore = useUserStore();
const appStore = useAppStore();

function renderCustomHeader() {
  return h(
      'div',
      {
        style: 'display: flex; align-items: center; padding: 8px 12px;'
      },
      [
        h(NAvatar, {
          round: true,
          style: 'margin-right: 12px;',
          src: 'https://07akioni.oss-cn-beijing.aliyuncs.com/demo1.JPG'
        }),
        h('div', null, [
          h('div', null, [h(NText, {depth: 2}, {default: () => '打工仔'})]),
          h('div', {style: 'font-size: 12px;'}, [
            h(
                NText,
                {depth: 3},
                {default: () => '毫无疑问，你是办公室里最亮的星'}
            )
          ])
        ])
      ]
  )
}

const options = [
  {
    key: 'header',
    type: 'render',
    render: renderCustomHeader
  },
  {
    label: t('personal.center'),
    key: 'profile',
    icon: () => h('div', {class: 'i-carbon-user-certification'})
  },
  {
    label: t('personal.switchOrg'),
    key: 'switchOrg',
    icon: () => h('div', {class: 'i-carbon-shuffle'})
  },
  {
    key: 'header-divider',
    type: 'divider'
  },
  {
    label: t('personal.exit'),
    key: 'logout',
    icon: () => h('div', {class: 'i-carbon-logout'})
  },
]
const logout = async (logoutTo?: string, noRedirect?: boolean) => {
  try {
    await userStore.logout()
    appStore.setTopMenus([])
    const currentRoute = router.currentRoute.value;
    window.$message.success(t('message.logoutSuccess'));
    router.push({
      name: logoutTo && true ? logoutTo : 'login',
      query: noRedirect
          ? {}
          : {
            ...router.currentRoute.value.query,
            redirect: currentRoute.name as string,
          },
    });
  } catch (error) {
    console.log(error);
  }
}
const handleSelect = (key: string) => {
  switch (key) {
    case 'profile':
      break;
    case 'switchOrg':
      break;
    case 'logout':
      window.$dialog.warning({
        title: t('personal.exit'),
        positiveText: t('personal.exit'),
        onPositiveClick: () => {
          logout()
        }
      })
      break;
  }
}
</script>

<template>
  <n-dropdown trigger="hover" :options="options" @select="handleSelect">
    <n-button text>{{ userStore.name }}</n-button>
  </n-dropdown>
</template>

<style scoped>

</style>