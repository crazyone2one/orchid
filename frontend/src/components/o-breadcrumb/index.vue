<script setup lang="ts">
import {NBreadcrumb, NBreadcrumbItem} from "naive-ui";
import {useAppStore} from "/@/store";
import {useRoute, useRouter} from "vue-router";
import {ref} from "vue";
import {listenerRouteChange} from "/@/utils/route-listener.ts";
import {BreadcrumbItem} from "/@/components/o-breadcrumb/types.ts";

const appStore = useAppStore();
const router = useRouter();
const route = useRoute();
const isEdit = ref(true);

/**
 * 监听路由变化，存储打开及选中的菜单
 */
listenerRouteChange((newRoute) => {
  const {name, meta} = newRoute;
  isEdit.value = false;
  // 顶部菜单层级会全等
  if (name === appStore.currentTopMenu.name) {
    appStore.setBreadcrumbList(appStore.currentTopMenu?.meta?.breadcrumbs);
  } else if ((name as string).includes(appStore.currentTopMenu.name as string)) {
    // 顶部菜单内下钻的父子路由命名是包含关系，子路由会携带完整的父路由名称
    const currentBreads = meta.breadcrumbs;
    appStore.setBreadcrumbList(currentBreads);
    // 下钻的三级路由一般都会区分编辑添加场景，根据场景展示不同的国际化路由信息
    const editTag = currentBreads && currentBreads[currentBreads.length - 1].editTag;
    setTimeout(() => {
      // 路由异步挂载，这里使用同步或者nextTick都取不到变化后的路由参数，所以使用定时器
      isEdit.value = editTag && route.query[editTag];
    }, 100);
  } else {
    appStore.setBreadcrumbList([]);
  }
}, true);

function jumpTo(crumb: BreadcrumbItem, index: number) {
  // 点击当前页面的面包屑，不跳转
  if (index === appStore.breadcrumbList.length - 1) {
    return;
  }
  if (crumb.isBack && window.history.state.back) {
    router.back();
  } else {
    const query: Record<string, any> = {};
    if (crumb.query) {
      crumb.query.forEach((key) => {
        query[key] = route.query[key];
      });
    }
    router.replace({name: crumb.name, query});
  }
}
</script>

<template>
  <n-breadcrumb v-if="appStore.breadcrumbList.length > 0" class="z-10 bg-lime-50 bg-left">
    <n-breadcrumb-item v-for="(crumb, index) of appStore.breadcrumbList"
                       :key="crumb.name" @click="jumpTo(crumb, index)">
      {{ isEdit ? $t(crumb.editLocale || crumb.locale) : $t(crumb.locale) }}
    </n-breadcrumb-item>
  </n-breadcrumb>
</template>

<style scoped>

</style>