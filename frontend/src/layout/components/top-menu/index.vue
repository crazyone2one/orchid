<script setup lang="ts">
import {useAppStore} from "/@/store";
import {h, ref, watch} from "vue";
import {listenerRouteChange} from "/@/utils/route-listener.ts";
import {RouteRecordRaw, RouterLink} from "vue-router";
import appClientMenus from "/@/router/app-menu";
import {cloneDeep} from "lodash-es";
import usePermission from "/@/hooks/use-permission.ts";
import type {MenuOption} from "naive-ui";
import {useI18n} from "vue-i18n";

const {t} = useI18n()
const appStore = useAppStore()
const activeKey = ref(null)
const copyRouters = cloneDeep(appClientMenus) as RouteRecordRaw[];
const permission = usePermission();
const menuOptions = ref<Array<MenuOption>>([])
const checkAuthMenu = () => {
  const topMenus = appStore.getTopMenus();
  appStore.setTopMenus(topMenus);
}
const setCurrentTopMenu = (key: string) => {
  // 先判断全等，避免同级路由出现命名包含情况
  const secParentFullSame = appStore.topMenus.find((route: RouteRecordRaw) => {
    return key === route?.name;
  });

  // 非全等的情况下，一定是父子路由包含关系
  const secParentLike = appStore.topMenus.find((route: RouteRecordRaw) => {
    return key.includes(route?.name as string);
  });

  if (secParentFullSame) {
    appStore.setCurrentTopMenu(secParentFullSame);
  } else if (secParentLike) {
    appStore.setCurrentTopMenu(secParentLike);
  }
}
listenerRouteChange(newRoute => {
  const {name} = newRoute;
  for (let i = 0; i < copyRouters.length; i++) {
    const firstRoute = copyRouters[i];
    // 权限校验通过
    if (permission.accessRouter(firstRoute)) {
      if (name && firstRoute?.name && (name as string).includes(firstRoute.name as string)) {
        // 先判断二级菜单是否顶部菜单
        let currentParent = firstRoute?.children?.some((item) => item.meta?.isTopMenu)
            ? (firstRoute as RouteRecordRaw)
            : undefined;

        if (!currentParent) {
          // 二级菜单非顶部菜单，则判断三级菜单是否有顶部菜单
          currentParent = firstRoute?.children?.find(
              (item) => name && item?.name && (name as string).includes(item.name as string)
          );
        }

        const filterMenuTopRouter =
            currentParent?.children?.filter((item: any) => permission.accessRouter(item) && item.meta?.isTopMenu) || [];
        menuOptions.value = []
        filterMenuTopRouter.map((route: RouteRecordRaw) => {
          menuOptions.value.push({
            // label: t(route.meta?.locale as string),
            label: () => h(RouterLink, {to: {name: route.name}}, {default: () => t(route.meta?.locale as string)}),
            key: route.name as string,
          })
        })
        appStore.setTopMenus(filterMenuTopRouter);
        setCurrentTopMenu(name as string);
        return;
      }
    }
  }
  // 切换到没有顶部菜单的路由时，清空顶部菜单
  appStore.setTopMenus([]);
  setCurrentTopMenu('');
}, true)
watch(() => appStore.getCurrentTopMenu().name, (value) => {
  checkAuthMenu();
  activeKey.value = value;
},{
  immediate: true,
})
</script>

<template>
  <div v-show="appStore.getTopMenus().length>0">
    <n-menu
        v-model:value="activeKey"
        mode="horizontal"
        :options="menuOptions"
        responsive
    />
  </div>
</template>

<style scoped>

</style>