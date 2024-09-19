<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue';
import {useI18n} from "vue-i18n";
import {ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {RouteEnum} from "/@/enums/route-enum.ts";
import OCardList from '/@/components/o-card-list/index.vue'

const {t} = useI18n();
const router = useRouter();
const route = useRoute();
const isNextTip = ref<boolean>(false);
const countDown = ref<number>(5);
const timer = ref<any>(null);
const cardList = ref([
  // {
  //   key: 'testPlanTemplate',
  //   name: t('caseManagement.featureCase.createTestPlan'),
  // },
  {
    key: 'caseReview',
    name: t('caseManagement.featureCase.createCaseReview'),
    route: RouteEnum.CASE_MANAGEMENT_REVIEW_CREATE,
  },
]);
const continueCreate = () => {
  router.push({
    name: RouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
  });
}
const goDetail = () => {
  router.push({
    name: RouteEnum.CASE_MANAGEMENT_CASE,
    query: route.query,
  });
}
const backCaseList = () => {
  router.push({
    name: RouteEnum.CASE_MANAGEMENT_CASE,
  });
}
const goNavigation = (name: string) => {
  router.push({
    name,
  });
}
</script>

<template>
  <o-card simple>
    <div class="h-full">
      <div class="mt-8 text-center">
        <div class="flex justify-center">
          <div class="i-my-icons:success w-[60px] h-[60px]"/>
        </div>
        <div class="mb-2 mt-6 text-[20px] font-medium"> {{ t('caseManagement.featureCase.addSuccess') }}</div>
        <div>
          <span class="mr-1 text-[rgb(64,128,255)]">{{ countDown }}</span>
          <span class="text-[var(--color-text-4)]">{{ t('caseManagement.featureCase.countDownTip') }}</span>
        </div>
        <div class="my-6">
          <n-button type="primary" @click="goDetail"> {{ t('caseManagement.featureCase.caseDetail') }}</n-button>
          <n-button class="mx-3" @click="continueCreate">
            {{ t('caseManagement.featureCase.addContinueCreate') }}
          </n-button>
          <n-button secondary @click="backCaseList">
            {{ t('caseManagement.featureCase.backCaseList') }}
          </n-button>
        </div>
        <n-checkbox v-model:checked="isNextTip" class="mb-6">{{
            t('caseManagement.featureCase.notNextTip')
          }}
        </n-checkbox>
      </div>
      <div>
        <div class="mb-4 font-medium">{{ t('caseManagement.featureCase.mightWantTo') }}</div>
        <o-card-list mode="static"
                     :card-min-width="569"
                     class="flex-1"
                     :shadow-limit="50"
                     :list="cardList"
                     :is-proportional="false"
                     :gap="16"
                     padding-bottom-space="16px">
          <template #item="{ item }">
            <div class="outerWrapper p-[3px]">
              <div class="innerWrapper flex items-center justify-between">
                <div class="flex items-center">
                  <div class="logo-img flex h-[48px] w-[48px] items-center justify-center">
                    <div class="i-my-icons:case-review h-[36px] w-[36px]"></div>
                  </div>
                  <div class="ml-2"> {{ item.name }}</div>
                </div>

                <n-button @click="goNavigation(item.route)">
                  {{ t('caseManagement.featureCase.addContinueCreate') }}
                </n-button>
              </div>
            </div>
          </template>
        </o-card-list>
      </div>
    </div>
  </o-card>
</template>

<style scoped>

</style>