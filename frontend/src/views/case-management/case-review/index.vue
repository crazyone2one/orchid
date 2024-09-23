<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue'
import ModuleTree from "/src/views/case-management/case-review/components/index/ModuleTree.vue";
import {ref, unref, watch} from "vue";
import {ModuleTreeNode} from "/@/models/common.ts";
import ReviewTable from "/@/views/case-management/case-review/components/index/ReviewTable.vue";
import {CaseManagementRouteEnum} from "/@/enums/route-enum.ts";
import {useRouter} from "vue-router";
import {ReviewListQueryParams} from "/@/models/case-management/case-review.ts";


type ShowType = 'all' | 'reviewByMe' | 'createByMe';
const router = useRouter();
const reviewTableRef = ref<InstanceType<typeof ReviewTable>>();
const showType = ref<ShowType>('all');
const folderTreeRef = ref<InstanceType<typeof ModuleTree>>();
const modulesCount = ref<Record<string, number>>({});
const moduleTree = ref<ModuleTreeNode[]>([]);
const moduleTreePathMap = ref<Record<string, any>>({});
const activeFolderId = ref<string>('all');
const offspringIds = ref<string[]>([]);
const initModuleTree = (tree: ModuleTreeNode[], pathMap: Record<string, any>) => {
  moduleTree.value = unref(tree);
  moduleTreePathMap.value = pathMap;
}
const handleFolderNodeSelect = (ids: string[], _offspringIds: string[]) => {
  [activeFolderId.value] = ids;
  offspringIds.value = [..._offspringIds];
}
const goCreateReview = () => {
  router.push({
    name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_CREATE,
    query:
        activeFolderId.value === 'all'
            ? {}
            : {
              moduleId: activeFolderId.value,
            },
  });
}
const initModuleCount = (params: ReviewListQueryParams) => {
  console.log(params)
}
</script>

<template>
  <o-card simple>
    <n-split :max="0.9" :min="0.1" :default-size="0.2">
      <template #1>
        <div class="p-[16px]">
          <module-tree ref="folderTreeRef"
                       :show-type="showType"
                       :modules-count="modulesCount"
                       :is-expand-all="true"
                       @folder-node-select="handleFolderNodeSelect"
                       @init="initModuleTree"
                       @create="goCreateReview"
          />
        </div>
      </template>
      <template #2>
        <review-table ref="reviewTableRef"
                      v-model:show-type="showType"
                      :active-folder="activeFolderId"
                      :module-tree="moduleTree"
                      :tree-path-map="moduleTreePathMap"
                      :offspring-ids="offspringIds"
                      @go-create="goCreateReview"
                      @init="initModuleCount"/>
      </template>
    </n-split>
  </o-card>
</template>

<style scoped>

</style>