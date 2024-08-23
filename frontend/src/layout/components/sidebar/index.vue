<script setup lang="ts">

import type {MenuOption} from "naive-ui";
import {NIcon, NLayoutSider, NMenu} from "naive-ui";
import {h, ref} from "vue";
import {useI18n} from "vue-i18n";
import {
  BugManagementRouteEnum,
  CaseManagementRouteEnum,
  ProjectManagementRouteEnum,
  SettingRouteEnum,
  TestPlanRouteEnum
} from "/@/enums/route-enum.ts";
import {RouteRecordRaw, RouterLink} from "vue-router";
import {listenerRouteChange} from "/@/utils/route-listener.ts";
import useMenuTree from "/@/layout/components/sidebar/use-menu-tree.ts";

const {menuTree} = useMenuTree();

const {t} = useI18n()
const renderIcon = (icon: string) => {
  return () => h(NIcon, null, {default: () => h('div', {class: icon}, {})})
}
const menuOptions: MenuOption[] = [
  {
    label: t('menu.projectManagement'),
    key: ProjectManagementRouteEnum.PROJECT_MANAGEMENT,
    icon: renderIcon('i-carbon-ibm-cloud-projects'),
    children: [
      {
        label: t('menu.projectManagement.projectPermission'),
        key: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION,
      },
      {
        label: t('project.permission.member'),
        key: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
      }
    ]
  },
  {
    label: t('menu.testPlan'),
    key: TestPlanRouteEnum.TEST_PLAN,
    icon: renderIcon('i-carbon-research-hinton-plot'),
    children: [
      {
        label: t('menu.apiTest.report'),
        key: TestPlanRouteEnum.TEST_PLAN_REPORT,
      }
    ]
  },
  {
    // label: t('menu.caseManagement'),
    label: () => h(RouterLink, {to: {name: CaseManagementRouteEnum.CASE_MANAGEMENT}}, {default: () => t('menu.caseManagement')}),
    key: CaseManagementRouteEnum.CASE_MANAGEMENT,
    icon: renderIcon('i-carbon-rule-test'),
    // children: [
    //   {
    //     label: t('menu.caseManagementShort'),
    //     key: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE,
    //   },
    //   {
    //     label: t('menu.caseManagement.caseManagementReviewShort'),
    //     key: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW,
    //   }
    // ]
  },
  {
    label: () => h(RouterLink, {to: {name: BugManagementRouteEnum.BUG_MANAGEMENT}}, {default: () => t('menu.bugManagement')}),
    key: BugManagementRouteEnum.BUG_MANAGEMENT,
    icon: renderIcon('i-carbon-debug'),
  },
  {
    label: t('menu.settings'),
    key: SettingRouteEnum.SETTING,
    icon: renderIcon('i-carbon-settings-services'),
    children: [
      {
        type: 'group',
        label: t('menu.settings.system'),
        key: SettingRouteEnum.SETTING_SYSTEM,
        children: [
          {
            // label: t('menu.settings.system.user'),
            label: () => h(RouterLink, {to: {name: SettingRouteEnum.SETTING_SYSTEM_USER_SINGLE}}, {default: () => t('menu.settings.system.user')}),
            key: SettingRouteEnum.SETTING_SYSTEM_USER_SINGLE,
          },
          {
            label: t('menu.settings.system.usergroup'),
            key: SettingRouteEnum.SETTING_SYSTEM_USER_GROUP,
            disabled: true
          },
          {
            label: t('menu.settings.system.organizationAndProject'),
            key: SettingRouteEnum.SETTING_SYSTEM_ORGANIZATION,
            disabled: true
          },
          {
            label: t('menu.settings.system.parameter'),
            key: SettingRouteEnum.SETTING_SYSTEM_PARAMETER,
            disabled: true
          },
          {
            label: t('menu.settings.system.resourcePool'),
            key: SettingRouteEnum.SETTING_SYSTEM_RESOURCE_POOL,
            disabled: true
          },
          {
            label: t('menu.settings.system.log'),
            key: SettingRouteEnum.SETTING_SYSTEM_LOG,
            disabled: true
          },
        ]
      },
      {
        type: 'group',
        label: t('menu.settings.organization'),
        key: SettingRouteEnum.SETTING_ORGANIZATION,
        children: [
          {
            // label: t('menu.settings.organization.member'),
            label: () => h(RouterLink, {to: {name: SettingRouteEnum.SETTING_ORGANIZATION_MEMBER}}, {default: () => t('menu.settings.organization.member')}),
            key: SettingRouteEnum.SETTING_ORGANIZATION_MEMBER,
          },
          {
            label: t('menu.settings.organization.userGroup'),
            key: SettingRouteEnum.SETTING_ORGANIZATION_USER_GROUP,
            disabled: true
          },
          {
            label: t('menu.settings.organization.project'),
            key: SettingRouteEnum.SETTING_ORGANIZATION_PROJECT,
            disabled: true
          },
          {
            label: t('menu.settings.organization.template'),
            key: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE,
            disabled: true
          },
          {
            label: t('menu.settings.organization.log'),
            key: SettingRouteEnum.SETTING_ORGANIZATION_LOG,
            disabled: true
          },
        ]
      },
    ],
  },
];
const expandedKeys = ref<string[]>([]);
const openKeys = ref<string[]>([]);
const findMenuOpenKeys = (target: string) => {
  const result: string[] = [];
  let isFind = false;
  const backtrack = (item: RouteRecordRaw | null, keys: string[]) => {
    if (target.includes(item?.name as string)) {
      result.push(...keys);
      if (result.length >= 2) {
        // 由于目前存在三级子路由，所以至少会匹配到三层才算结束
        isFind = true;
        return;
      }
    }
    if (item?.children?.length) {
      item.children.forEach((el) => {
        backtrack(el, [...keys, el.name as string]);
      });
    }
  };

  menuTree.value?.forEach((el: RouteRecordRaw | null) => {
    if (isFind) return; // 节省性能
    backtrack(el, [el?.name as string]);
  });
  return result;
};
listenerRouteChange((newRoute) => {

  const {requiresAuth, activeMenu, hideInMenu} = newRoute.meta;
  if (requiresAuth !== false && (!hideInMenu || activeMenu)) {
    const menuOpenKeys = findMenuOpenKeys((activeMenu || newRoute.name) as string);
    const keySet = new Set([...menuOpenKeys, ...openKeys.value]);
    openKeys.value = [...keySet];
    expandedKeys.value = [activeMenu || menuOpenKeys[menuOpenKeys.length - 1]];
  }
}, true);
</script>

<template>
  <n-layout-sider
      show-trigger
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      :native-scrollbar="false"
      bordered
  >
    <n-menu
        v-model:value="expandedKeys[0]"
        :default-expanded-keys="openKeys"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :options="menuOptions"
        accordion
    />
  </n-layout-sider>
</template>

<style scoped>

</style>