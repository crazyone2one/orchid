<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue';
import {useRoute, useRouter} from "vue-router";
import {useAppStore} from "/@/store";
import {useI18n} from "vue-i18n";
import {onBeforeMount, ref} from "vue";
import {BaseAssociateCaseRequest, ReviewPassRule} from "/@/models/case-management/case-review.ts";
import type {FormInst, SelectOption, TreeSelectOption} from "naive-ui";
import {useForm, useRequest} from "alova/client";
import {
  addReview,
  copyReview,
  getReviewDetail,
  getReviewModules,
  getReviewUsers
} from "/@/api/modules/case-management/case-review.ts";
import {CaseManagementRouteEnum} from "/@/enums/route-enum.ts";

const route = useRoute();
const router = useRouter();
const appStore = useAppStore();
const {t} = useI18n();
const formRef = ref<FormInst | null>(null)
const isEdit = ref(!!route.query.id);
const isCopy = ref(!!route.query.copyId);

const rules = {
  name: [{required: true, message: t('caseManagement.caseReview.reviewNameRequired')}],
  reviewers: [{required: true, message: t('caseManagement.caseReview.defaultReviewerRequired')}]
};
const moduleOptions = ref<TreeSelectOption []>([]);
const reviewersOptions = ref<SelectOption[]>([]);
const selectedAssociateCasesParams = ref<BaseAssociateCaseRequest>({
  excludeIds: [],
  selectIds: [],
  selectAll: false,
  condition: {},
  moduleIds: [],
  versionId: '',
  refId: '',
  projectId: '',
});
const {
  loading: moduleLoading,
  send: fetchReviewModules
} = useRequest(() => getReviewModules(appStore.currentProjectId), {immediate: false, force: true});
const {
  loading: reviewerLoading,
  send: fetchReviewUsers
} = useRequest(() => getReviewUsers(appStore.currentProjectId, ''), {immediate: false, force: true})
const {
  loading,
  send: fetchReviewDetail
} = useRequest(() => getReviewDetail((route.query.copyId as string) || (route.query.id as string) || ''), {
  immediate: false,
  force: true
})
const initModules = () => {
  fetchReviewModules().then(res => moduleOptions.value = res as unknown as TreeSelectOption[])
}
const initReviewers = () => {
  fetchReviewUsers().then(res => reviewersOptions.value = res.map((e) => ({
    label: e.name,
    value: e.id,
    avatar: e.avatar
  })));
}
const initReviewDetail = () => {
  fetchReviewDetail().then(res => {
    form.value = {
      name: res.name,
      desc: res.description,
      folderId: res.moduleId,
      type: res.reviewPassRule,
      reviewers: res.reviewers.map((e) => e.userId),
      tags: res.tags,
      cycle: [res.startTime, res.endTime],
      caseCount: res.caseCount,
    };
  })
}
const {form, send: addReviewForm, loading: saveLoading} = useForm(formData => {
      const {name, folderId, type, cycle, tags, desc, reviewers} = formData as any;
      if (isCopy.value) {
        // 编辑评审场景、复制评审场景
        return copyReview({
          copyId: route.query.copyId as string,
          projectId: appStore.currentProjectId,
          name,
          moduleId: folderId,
          reviewPassRule: type, // 评审通过规则
          startTime: cycle ? cycle[0] : null,
          endTime: cycle ? cycle[1] : null,
          tags,
          description: desc,
          reviewers, // 评审人员
        })
      } else {
        return addReview({
          projectId: appStore.currentProjectId,
          name,
          moduleId: folderId,
          reviewPassRule: type, // 评审通过规则
          startTime: cycle ? cycle[0] : null,
          endTime: cycle ? cycle[1] : null,
          tags,
          description: desc,
          reviewers, // 评审人员
          baseAssociateCaseRequest: selectedAssociateCasesParams.value, // 关联用例
        })
      }
    },
    {
      // 初始化表单数据
      initialForm: {
        name: '',
        desc: '',
        folderId: (route.query.moduleId as string) || 'root',
        type: 'SINGLE' as ReviewPassRule,
        reviewers: [] as string[],
        tags: [] as string[],
        cycle: [Date.now(), Date.now()],
        caseCount: 0,
      }
    })
const saveReview = (isGoReview = false) => {
  formRef.value?.validate(errors => {
    if (!errors) {
      addReviewForm().then(res => {
        window.$message.success(t('common.createSuccess'));
        if (isGoReview) {
          // 是否去评审，是的话先保存然后直接跳转至该评审详情页进行评审
          router.replace({
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL,
            query: {
              id: res.id,
            },
          });
        } else {
          router.replace({
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW,
          });
        }
      })
    }
  })
}
const handleCancel = () => {
  router.back()
}
onBeforeMount(() => {
  initModules();
  initReviewers();
  if (isEdit.value || isCopy.value) {
    // 编辑评审场景、复制评审场景初始化评审数据
    initReviewDetail();
  }
})
</script>

<template>
  <o-card
      :title="isEdit ? t('menu.caseManagement.caseManagementCaseReviewEdit') : t('caseManagement.caseReview.create')">
    <n-form ref="formRef" :model="form" label-placement="top" :rules="rules">
      <n-form-item :label="t('caseManagement.caseReview.reviewName')" path="name">
        <n-input v-model:value="form.name" :placeholder="t('caseManagement.caseReview.reviewNamePlaceholder')"
                 :maxlength="255"/>
      </n-form-item>
      <n-form-item :label="t('common.desc')" path="desc">
        <n-input v-model:value="form.desc"
                 type="textarea"
                 :placeholder="t('caseManagement.caseReview.descPlaceholder')"
                 :maxlength="1000"/>
      </n-form-item>
      <n-form-item :label="t('caseManagement.caseReview.belongModule')" path="folderId">
        <n-tree-select v-model:value="form.folderId" :options="moduleOptions"
                       :loading="moduleLoading"
                       :placeholder="t('caseManagement.caseReview.belongModulePlaceholder')"
                       label-field="name"
                       key-field="id"
                       class="w-[436px]" filterable/>
      </n-form-item>
      <n-form-item v-if="!isEdit" :label="t('caseManagement.caseReview.type')" path="type">
        <n-radio-group v-model:value="form.type">
          <n-radio value="SINGLE">
            <div class="flex items-center">
              {{ t('caseManagement.caseReview.single') }}
              <n-tooltip>
                <template #trigger>
                  <div class="i-my-icons:question-circle"/>
                </template>
                {{ t('caseManagement.caseReview.singleTip') }}
              </n-tooltip>
            </div>
          </n-radio>
          <n-radio value="MULTIPLE">
            <div class="flex items-center">
              {{ t('caseManagement.caseReview.multi') }}
              <n-tooltip>
                <template #trigger>
                  <div class="i-my-icons:question-circle"/>
                </template>
                {{ t('caseManagement.caseReview.multiTip') }}
              </n-tooltip>
            </div>
          </n-radio>
        </n-radio-group>
      </n-form-item>
      <n-form-item :label="t('caseManagement.caseReview.defaultReviewer')" path="reviewers">
        <n-select v-model:value="form.reviewers" :options="reviewersOptions"
                  class="w-[436px]" :loading="reviewerLoading" multiple
                  :placeholder="t('caseManagement.caseReview.reviewerPlaceholder')"/>
        <span class="text-[var(--color-text-4)]">{{ t('caseManagement.caseReview.defaultReviewerTip') }}</span>
      </n-form-item>
      <n-form-item :label="t('caseManagement.caseReview.tag')" path="tags">
        <n-dynamic-tags v-model:value="form.tags"/>
      </n-form-item>
      <n-form-item :label="t('caseManagement.caseReview.cycle')" path="cycle">
        <n-date-picker v-model:value="form.cycle" type="daterange" clearable/>
      </n-form-item>
      <n-form-item v-if="!isEdit">
        <template #label>
          <div class="flex items-center">
            <div>{{ t('caseManagement.caseReview.pickCases') }}</div>
            <n-divider v-if="!isCopy" margin="4px" vertical/>
            <n-button
                v-if="!isCopy"
                text
                :disabled="selectedAssociateCasesParams.selectIds.length === 0"
            >
              {{ t('caseManagement.caseReview.clearSelectedCases') }}
            </n-button>
          </div>
        </template>
        <div class="bg-[var(--color-text-n9)] p-[12px]">
          <div class="flex items-center">
            <div class="text-[var(--color-text-2)]">
              {{
                t('caseManagement.caseReview.selectedCases', {
                  count: isCopy
                      ? form.caseCount
                      : selectedAssociateCasesParams.selectAll
                          ? selectedAssociateCasesParams.totalCount
                          : selectedAssociateCasesParams.selectIds.length,
                })
              }}
            </div>
            <n-divider v-if="!isCopy" margin="8px" vertical/>
            <n-button
                v-if="!isCopy"
                v-permission="['CASE_REVIEW:READ+RELEVANCE']"
                text
                class="font-medium"

            >
              {{ t('ms.case.associate.title') }}
            </n-button>
          </div>
        </div>
      </n-form-item>
    </n-form>
    <template #footerRight>
      <div class="flex items-center">
        <n-button secondary :disabled="saveLoading" @click="handleCancel">{{ t('common.cancel') }}</n-button>
        <n-button v-if="isEdit" v-permission="['CASE_REVIEW:READ+UPDATE']" type="primary" class="ml-[16px]"
                  :loading="saveLoading">
          {{ t('common.update') }}
        </n-button>
        <template v-else>
          <n-button v-permission="['CASE_REVIEW:READ+ADD']" secondary class="mx-[16px]"
                    :loading="saveLoading"
                    @click="() => saveReview()">
            {{ t('common.save') }}
          </n-button>
          <n-button v-permission.all="['CASE_REVIEW:READ+ADD', 'CASE_REVIEW:READ+REVIEW']" type="primary"
                    :loading="saveLoading"
                    @click="() => saveReview(true)">
            {{ t('caseManagement.caseReview.review') }}
          </n-button>
        </template>
      </div>
    </template>
  </o-card>
</template>

<style scoped>

</style>