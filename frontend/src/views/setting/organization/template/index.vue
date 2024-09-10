<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue'
import CardList from '/@/components/o-card-list/index.vue'
import TemplateItem from '/@/views/setting/organization/template/components/TemplateItem.vue';
import useTemplateStore from "/@/store/modules/setting/template.ts";
import {useRoute, useRouter} from "vue-router";
import {SettingRouteEnum} from "/@/enums/route-enum.ts";
import {onBeforeMount, ref} from "vue";
import {getCardList} from "/@/views/setting/organization/template/components/field-setting.ts";

const templateStore = useTemplateStore();
const router = useRouter();
const route = useRoute();
// 字段设置
const fieldSetting = (key: string) => {
  router.push({
    name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_FILED_SETTING,
    query: {
      ...route.query,
      type: key,
    },
  });
};
// 模板管理
const templateManagement = (key: string) => {
  router.push({
    name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT,
    query: {
      ...route.query,
      type: key,
    },
  });
};
const cardList = ref<Record<string, any>[]>([]);
// 更新状态列表
const updateState = () => {
  cardList.value = [...getCardList('organization')];
};

onBeforeMount(() => {
  updateState();
});
onBeforeMount(() => {
  templateStore.getStatus();
});
</script>

<template>
  <o-card simple>
    <div style="display: flex !important" class="flex h-[100%] flex-col overflow-hidden">
      <card-list mode="static" :card-min-width="360" :shadow-limit="50" class="flex-1"
                 :list="cardList"
                 :is-proportional="false" :gap="16"
                 padding-bottom-space="16px">
        <template #item="{ item, index }">
          <TemplateItem
              :card-item="item"
              :index="index"
              mode="organization"
              @field-setting="fieldSetting"
              @template-management="templateManagement"
              @update-state="updateState"
          />
        </template>
      </card-list>
    </div>
  </o-card>
</template>

<style scoped>

</style>