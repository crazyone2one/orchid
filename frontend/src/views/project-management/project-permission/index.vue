<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue'
import MenuPanel from "/@/layout/components/MenuPanel.vue";
import {useRoute, useRouter} from "vue-router";
import {ProjectManagementRouteEnum} from "/@/enums/route-enum.ts";
import usePermission from "/@/hooks/use-permission.ts";
import {computed, onBeforeMount, provide, ref} from "vue";
import {useI18n} from "vue-i18n";

const {t} = useI18n()
const router = useRouter();
const route = useRoute();
const permission = usePermission();
const memberPermissionShowCondition = () => {
  let show = false;
  const routerList = router.getRoutes();
  for (let i = 0; i < routerList.length; i++) {
    const rou = routerList[i];
    if (
        [
          ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
          ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_USER_GROUP,
        ].includes(rou.name as ProjectManagementRouteEnum)
    ) {
      show = permission.accessRouter(rou);
    }
    if (show) {
      break;
    }
  }
  return show;
}
const sourceMenuList = ref([
  {
    key: 'project',
    title: t('project.permission.project'),
    level: 1,
    name: '',
  },
  {
    key: 'projectBasicInfo',
    title: t('project.permission.basicInfo'),
    level: 2,
    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_BASIC_INFO,
  },
  {
    key: 'projectMenuManage',
    title: t('project.permission.menuManagement'),
    level: 2,
    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT,
  },
  // TODO 第一版不开启版本
  // {
  //   key: 'projectVersion',
  //   title: t('project.permission.projectVersion'),
  //   level: 2,
  //   name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_VERSION,
  // },
  {
    key: 'memberPermission',
    title: t('project.permission.memberPermission'),
    level: 1,
    name: '',
    showCondition: memberPermissionShowCondition,
  },
  {
    key: 'projectMember',
    title: t('project.permission.member'),
    level: 2,
    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
  },
  {
    key: 'projectUserGroup',
    title: t('project.permission.userGroup'),
    level: 2,
    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_USER_GROUP,
  },
]);
const currentKey = ref<string>('');
const menuList = computed(() => {
  const routerList = router.getRoutes();
  return sourceMenuList.value.filter((item) => {
    if (item.name) {
      const routerItem = routerList.find((rou) => rou.name === item.name);
      if (!routerItem) return false;
      if (routerItem.name === ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_VERSION) {
        return permission.accessRouter(routerItem);
      }
      return permission.accessRouter(routerItem);
    }
    return true;
  });
});
const toggleMenu = (itemName: string) => {
  if (itemName) {
    currentKey.value = itemName;
    router.push({name: itemName});
  }
};
const isLoading = ref(false);

const reload = (flag: boolean) => {
  isLoading.value = flag;
};
provide('reload', reload);

const setInitRoute = () => {
  if (route?.name) currentKey.value = route.name as string;
};
onBeforeMount(() => {
  setInitRoute();
  // licenseStore.getValidateLicense();
});
</script>

<template>
  <div class="wrapper flex min-h-[500px]" :style="{ height: 'calc(100vh - 74px)' }">
    <menu-panel :title="$t('project.permission.projectAndPermission')"
                :default-key="currentKey" :menu-list="menuList"
                class="mr-[16px] w-[208px] min-w-[208px] bg-white p-[16px]"
                @toggle-menu="toggleMenu"/>
    <o-card simple>
      <router-view/>
    </o-card>
  </div>
</template>

<style scoped>
.left-menu-wrapper {
  border-radius: 12px;
  color: rgb(29, 33, 41);
  box-shadow: 0 0 10px rgb(120 56 135/ 5%);

  .left-content {
    width: 100%;

    .menu {
      .menu-item {
        height: 38px;
        line-height: 38px;
      }
    }
  }
}

.right-menu-wrapper {
  border-radius: 12px;
  box-shadow: 0 0 10px rgb(120 56 135/ 5%);
  @apply bg-white;
}

.is-active {
  border-radius: 4px;
  color: rgb(64, 128, 255);
  background-color: rgb(232, 243, 255);
}

.rightTest {
  height: 100%;
}
</style>