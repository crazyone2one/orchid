<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue';
import {useI18n} from "vue-i18n";
import {ref} from "vue";

const {t} = useI18n();

const isNextTip = ref<boolean>(false);
const countDown = ref<number>(5);
const continueCreate = () => {

}
const goDetail = () => {

}
const backCaseList = () => {

}
</script>

<template>
  <o-card simple>
    <div class="h-full">
      <div class="mt-8 text-center">
        <div class="flex justify-center">
          <svg-icon :width="'60px'" :height="'60px'" :name="'success'"/>
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
        <n-checkbox v-model:value="isNextTip" class="mb-6">{{ t('caseManagement.featureCase.notNextTip') }}</n-checkbox>
      </div>
      <div>
        <div class="mb-4 font-medium">{{ t('caseManagement.featureCase.mightWantTo') }}</div>
      </div>
    </div>
  </o-card>
</template>

<style scoped>

</style>