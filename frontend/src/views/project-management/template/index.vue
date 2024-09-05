<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue'
import CardList from '/@/components/o-card-list/index.vue';
import {onBeforeMount, ref} from "vue";
import useTemplateStore from "/@/store/modules/setting/template.ts";
import {getCardList} from "/@/views/setting/organization/template/components/field-setting.ts";
import TemplateItem from "/@/views/setting/organization/template/components/TemplateItem.vue";
import {ProjectManagementRouteEnum} from "/@/enums/route-enum.ts";
import {useRoute, useRouter} from "vue-router";

const templateStore = useTemplateStore();
const router = useRouter();
const route = useRoute();
const cardList = ref<Record<string, any>[]>([]);
// 更新状态列表
const updateState = () => {
  cardList.value = [...getCardList('project')];
};
const fieldSetting = (key: string) => {
  router.push({
    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_FIELD_SETTING,
    query: {
      ...route.query,
      type: key,
    },
  });
};
// 模板管理
const templateManagement = (key: string) => {
  router.push({
    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT,
    query: {
      ...route.query,
      type: key,
    },
  });
};
onBeforeMount(() => {
  templateStore.getStatus();
  updateState();
});
</script>

<template>
  <o-card simple>
    <card-list mode="static"
               :card-min-width="360"
               class="flex-1"
               :shadow-limit="50"
               :list="cardList"
               :is-proportional="false"
               :gap="16"
               padding-bottom-space="16px">
      <template #item="{ item, index }">
        <template-item
            :card-item="item"
            :index="index"
            mode="project"
            @field-setting="fieldSetting"
            @template-management="templateManagement"
            @update-state="updateState"
        />
      </template>
    </card-list>
  </o-card>
</template>

<style scoped>

</style>