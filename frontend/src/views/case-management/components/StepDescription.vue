<script setup lang="ts">
import {useI18n} from "vue-i18n";

const {t} = useI18n();

const props = defineProps<{
  isTestPlan?: boolean;
}>();
const innerCaseEditType = defineModel<string>('caseEditType', {
  required: true,
});
// 更改类型
const handleSelectType = (value: string | number | Record<string, any> | undefined) => {
  innerCaseEditType.value = value as string;
};

// 获取类型样式
function getSelectTypeClass(type: string) {
  return innerCaseEditType.value === type ? ['bg-[rgb(232,243,255)]', '!text-[rgb(64,128,255)]'] : [];
}

const options = [
  {
    label: t('system.orgTemplate.stepDescription'),
    key: 'STEP',
    class: getSelectTypeClass('STEP')
  },
  {
    label: t('system.orgTemplate.textDescription'),
    key: 'TEXT',
    class: getSelectTypeClass('TEXT')
  },
]
</script>

<template>
  <div class="mb-[16px] flex items-center">
    <div class="font-bold text-[rgb(29,33,41)]">
      {{
        innerCaseEditType === 'STEP' ? t('system.orgTemplate.stepDescription') : t('system.orgTemplate.textDescription')
      }}
    </div>
    <div v-if="!props.isTestPlan" class="font-normal">
      <n-divider vertical/>
      <n-dropdown trigger="hover" :options="options" @select="handleSelectType">
        <span class="changeType">
          {{ t('system.orgTemplate.changeType') }}
          <div class="i-carbon-triangle-down-solid" />
        </span>
      </n-dropdown>
    </div>
  </div>
</template>

<style scoped>

</style>