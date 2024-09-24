<script setup lang="ts">
import CaseAssociate from '/@/components/o-case-associate/index.vue'
import {getCaseList, getCaseModuleTree} from "/@/api/modules/case-management/feature-case.ts";
import {RequestModuleEnum} from "/@/components/o-case-associate/utils.ts";
import {BaseAssociateCaseRequest} from "/@/models/case-management/case-review.ts";
import {useVModel} from "@vueuse/core";
import {ref} from "vue";
import {CaseLinkEnum} from "/@/enums/case-enum.ts";
import {useI18n} from "vue-i18n";
import type {FormInst, SelectOption} from "naive-ui";
import {ProjectManagementRouteEnum} from "/@/enums/route-enum.ts";
import {useRouter} from "vue-router";

const props = defineProps<{
  visible: boolean;
  project: string;
  reviewId?: string;
  reviewers?: string[];
  hasNotAssociatedIds?: string[];
  reviewersOptions: SelectOption[]
}>();
const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void;
  (e: 'update:project', val: string): void;
  (e: 'update:associatedIds', val: string[]): void;
  (e: 'success', val: BaseAssociateCaseRequest & { reviewers: string[] }): void;
  (e: 'close'): void;
}>();
const {t} = useI18n()
const router = useRouter()
const innerVisible = useVModel(props, 'visible', emit);
const innerProject = useVModel(props, 'project', emit);
const currentSelectCase = ref<CaseLinkEnum>(CaseLinkEnum.FUNCTIONAL);
const confirmLoading = ref<boolean>(false);
const associateFormRef = ref<FormInst | null>(null);
const associateForm = ref({
  reviewers: props.reviewers || ([] as string[]),
});
const goProjectManagement = () => {
  window.open(
      `${window.location.origin}#${router.resolve({name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_USER_GROUP}).fullPath}`
  );
}
const handleSave = (params: BaseAssociateCaseRequest) => {
  associateFormRef.value?.validate(errors => {
    try {
      if (!errors) {
        confirmLoading.value = true;
        // associatedIds.value = [...params.selectIds];
        emit('success', { ...params, reviewers: associateForm.value.reviewers });
        innerVisible.value = false;
      }
    }catch (error) {
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  })
}
</script>

<template>
  <case-associate v-model:visible="innerVisible"
                  :current-select-case="currentSelectCase"
                  :project-id="innerProject"
                  :get-modules-func="getCaseModuleTree"
                  :get-table-func="getCaseList"
                  :confirm-loading="confirmLoading"
                  :associated-ids="[]"
                  :has-not-associated-ids="props.hasNotAssociatedIds"
                  :type="RequestModuleEnum.CASE_MANAGEMENT"
                  :table-params="{ reviewId: props.reviewId }"
                  hide-project-select
                  :reviewers-options="props.reviewersOptions"
                  @close="emit('close')"
                  @save="handleSave">
    <template #footerLeft>
      <n-form ref="associateFormRef" :model="associateForm">
        <n-form-item>
          <template #label>
            <div class="inline-flex items-center">
              {{ t('caseManagement.caseReview.reviewer') }}
              <n-tooltip placement="right">
                <template #trigger>
                  <div
                      class="i-my-icons:question-circle ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(64,128,255)]"
                      size="5"
                  />
                </template>
                <div>{{ t('caseManagement.caseReview.switchProject') }}</div>
                <div>{{ t('caseManagement.caseReview.resetReviews') }}</div>
                <div>
                  {{ t('caseManagement.caseReview.reviewsTip') }}
                  <span class="cursor-pointer text-[rgb(106,161,255)]" @click="goProjectManagement">
                      {{ t('menu.projectManagement') }}
                    </span>
                  {{ t('caseManagement.caseReview.reviewsTip2') }}
                </div>
              </n-tooltip>
            </div>
          </template>
          <n-select v-model:value="associateForm.reviewers"
                    :placeholder="t('caseManagement.caseReview.reviewerPlaceholder')"
                    :options="props.reviewersOptions"
                    multiple
                    class="!w-[290px]"/>
        </n-form-item>
      </n-form>
    </template>
  </case-associate>
</template>

<style scoped>

</style>