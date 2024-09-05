<script setup lang="ts">
import {useAppStore} from "/@/store";
import {computed, inject, onBeforeMount, ref} from "vue";
import {ProjectBasicInfoModel} from "/@/models/project-management/basicInfo.ts";
import dayjs from 'dayjs';
import {useRequest} from "alova/client";
import {getProjectInfo} from "/@/api/modules/project-management/basic-info.ts";

const appStore = useAppStore();

const currentProjectId = computed(() => appStore.currentProjectId);

const updateLoading = inject('reload', (flag: boolean) => {
});
const isVisible = ref<boolean>(false);
const projectDetailRef = ref();

const editHandler = () => {
  isVisible.value = true;
  projectDetailRef.value.editProject(projectDetail.value);
};
const projectDetail = ref<ProjectBasicInfoModel>();
const {send: getProjectDetail} = useRequest((id) => getProjectInfo(id), {immediate: false})
onBeforeMount(() => {
  getProjectDetail(currentProjectId.value).then(res => projectDetail.value = res)
})
</script>

<template>
  <div class="wrapper mb-6 flex justify-between">
    <span class="font-medium text-[var(--color-text-000)]">{{ $t('project.basicInfo.basicInfo') }}</span>
    <n-button
        v-show="!projectDetail?.deleted"
        v-permission="['PROJECT_BASE_INFO:READ+UPDATE']"
        @click="editHandler"
    >
      {{ $t('project.basicInfo.edit') }}
    </n-button>
  </div>
  <div class="project-info mb-6 h-[112px] bg-white p-1">
    <div class="inner-wrapper rounded-md p-4">
      <div class="detail-info flex flex-col justify-between rounded-md p-4">
        <div class="flex items-center">
          <span class="one-line-text mr-1 max-w-[300px] font-medium text-[var(--color-text-000)]">
            {{ projectDetail?.name }}
          </span>
          <span class="button mr-1" :class="[projectDetail?.deleted ? 'delete-button' : 'enable-button']">
            {{ projectDetail?.deleted ? $t('project.basicInfo.deleted') : $t('project.basicInfo.enable') }}
          </span>
        </div>
        <div class="one-line-text text-[12px] text-[--color-text-4]">{{ projectDetail?.description }}</div>
      </div>
    </div>
  </div>
  <div class="ml-1 flex flex-col">
    <div class="label-item">
      <span class="label">{{ $t('project.basicInfo.createBy') }}</span>
      <span>{{ projectDetail?.createUser }}</span>
    </div>
    <div class="label-item">
      <span class="label">{{ $t('project.basicInfo.organization') }}</span>
      <n-tag>{{ projectDetail?.organizationName }}</n-tag>
    </div>
    <div class="label-item">
      <span class="label">{{ $t('project.basicInfo.resourcePool') }}</span>
      <n-tag v-for="pool of projectDetail?.resourcePoolList" :key="pool.id">{{ pool.name }}</n-tag>
    </div>
    <div class="label-item">
      <span class="label">{{ $t('project.basicInfo.createTime') }}</span>
      <span>{{ dayjs(projectDetail?.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
    </div>
  </div>
</template>

<style scoped>
.project-info {
  border-radius: 4px;
  box-shadow: 0 0 10px rgb(120 56 135/5%);

  .inner-wrapper {
    height: 100%;
    background: rgb(232, 243, 255);

    .detail-info {
      height: 100%;
      background: url('../../../../assets/images/basic_bg.png');
      background-size: cover;

      .button {
        font-size: 12px;
        border-radius: 2px;
        line-height: 20px;
        @apply inline-block px-2 py-1;
      }

      .enable-button {
        color: rgb(35, 195, 67);
        background: rgb(232, 255, 234);
      }

      .disabled-button {
        color: rgb(201, 205, 212);
        background: rgb(78, 89, 105);
      }

      .delete-button {
        color: rgb(247, 101, 96);
        background: rgb(255, 236, 232);
      }
    }
  }
}

.label-item {
  margin-bottom: 16px;
  height: 22px;
  line-height: 22px;

  span {
    float: left;
  }

  .label {
    margin-right: 16px;
    width: 120px;
    color: rgb(247, 101, 96);
  }
}
</style>