<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue';
import {computed, ref, toRef, watchEffect} from "vue";
import {useRoute, useRouter} from "vue-router";
import {useI18n} from "vue-i18n";
import useFeatureCaseStore from "/@/store/modules/case/feature-case.ts";
import useLeaveUnSaveTip from "/@/hooks/use-leave-unsave-tip.ts";
import CaseTemplateDetail from '/@/views/case-management/case-management-feature/components/CaseTemplateDetail.vue'
import {CreateOrUpdateCase} from "/@/models/case-management/feature-case.ts";
import {useRequest} from "alova/client";
import {createCaseRequest} from "/@/api/modules/case-management/feature-case.ts";
import {CaseManagementRouteEnum} from "/@/enums/route-enum.ts";

const {t} = useI18n();
const route = useRoute();
const router = useRouter();
const featureCaseStore = useFeatureCaseStore();
const {setIsSave} = useLeaveUnSaveTip();
const isEdit = computed(() => !!route.query.id);
const isFormReviewCase = computed(() => route.query.reviewId);
const caseTemplateDetailRef = ref<InstanceType<typeof CaseTemplateDetail> | null>(null);
const createSuccessId = ref<string>('');
const okText = ref<string>('');
const title = ref('');
const caseDetailInfo = ref({
  request: {} as CreateOrUpdateCase,
  fileList: [],
});
const isShowTip = ref<boolean>(true);
const {loading, send: createCase} = useRequest((param) => createCaseRequest(param), {immediate: false})
const handleSave = (isContinue = false, isReview = false) => {
  caseTemplateDetailRef.value?.validForm()
  if (route.params.mode === 'edit') {

  } else {
    // 创建并关联
    if (isReview) {
      caseDetailInfo.value.request.reviewId = route.query.reviewId;
    }
    createCase(caseDetailInfo.value).then(res => {
      if (isContinue) {
        window.$message.success(t('caseManagement.featureCase.addSuccess'));
        caseTemplateDetailRef.value?.resetForm();
        return;
      }
      createSuccessId.value = res.id;

      if (isReview) {
        window.$message.success(t('caseManagement.featureCase.createAndLinkSuccess'));
        setIsSave(true);
        router.back();
        return;
      }
      window.$message.success(route.params.mode === 'copy' ? t('ms.description.copySuccess') : t('common.addSuccess'));
      if (isShowTip.value && !route.query.id) {
        router.push({
          name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_CREATE_SUCCESS,
          query: {
            id: createSuccessId.value,
            ...route.query,
          },
        });
      } else {
        router.push({
          name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE,
          query: {
            orgId: route.query.orgId,
            pId: route.query.pId,
          },
        });
      }
      setIsSave(true);
    })
  }
};

watchEffect(() => {
  if (route.params.mode === 'edit') {
    title.value = t('caseManagement.featureCase.updateCase');
    okText.value = t('mscard.defaultUpdate');
  } else if (route.params.mode === 'copy') {
    title.value = t('caseManagement.featureCase.copyCase');
    okText.value = t('mscard.defaultConfirm');
  } else {
    title.value = t('caseManagement.featureCase.creatingCase');
    okText.value = t('mscard.defaultConfirm');
  }
});
</script>

<template>
  <o-card has-breadcrumb :title="title" :is-edit="isEdit">
    <case-template-detail ref="caseTemplateDetailRef"
                          v-model:form-mode-value="caseDetailInfo"
                          :case-id="(route.query.id as string || '')"
    />
    <template #footerRight>
      <div class="flex justify-end gap-[16px]">
        <n-button secondary>{{ t('mscard.defaultCancelText') }}</n-button>
        <n-button v-if="!isEdit" secondary>{{ t('mscard.defaultSaveAndContinueText') }}</n-button>
        <n-button v-if="!isFormReviewCase" type="primary" @click="handleSave(false)">
          {{ okText }}
        </n-button>
        <n-button v-if="!isFormReviewCase" type="primary">
          {{ t('caseManagement.featureCase.createAndLink') }}
        </n-button>
      </div>
    </template>
  </o-card>

</template>

<style scoped>

</style>