<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue'
import {nextTick, onBeforeMount, ref} from "vue";
import SystemProject from '/@/views/setting/system/organizationAndProject/components/SystemProject.vue';
import SystemOrganization from '/@/views/setting/system/organizationAndProject/components/SystemOrganization.vue'
import AddProjectModal from "/@/views/setting/system/organizationAndProject/components/AddProjectModal.vue";
import {getOrgAndProjectCount} from "/@/api/modules/setting/system-org-project.ts";

const currentTable = ref('organization');
const orgTableRef = ref<InstanceType<typeof SystemOrganization> | null>(null)
const projectTableRef = ref<InstanceType<typeof SystemProject> | null>(null)
const addProjectModalRef = ref<InstanceType<typeof AddProjectModal> | null>(null);
const organizationCount = ref(0);
const projectCount = ref(0);

const handleAddOrganization = () => {
  if (currentTable.value === 'organization') {

  } else {
    projectVisible.value = true
  }
}
const projectVisible = ref(false);
const handleAddProjectCancel = (search: boolean) => {
  projectVisible.value = false
  if (search) {
    tableSearch()
  }
}
const initOrgAndProjectCount = async () => {
  try {
    const res = await getOrgAndProjectCount();
    organizationCount.value = res.organizationTotal;
    projectCount.value = res.projectTotal;
  } catch (error) {
    console.error(error);
  }
}
const tableSearch = () => {
  if (currentTable.value === 'organization') {
    // orgTableRef.value?.tableSearch()
  } else if (projectTableRef.value) {
    projectTableRef.value.fetchData();
  } else {
    nextTick(() => {
      projectTableRef.value?.fetchData();
    });
  }
  initOrgAndProjectCount()
}
onBeforeMount(() => {
  tableSearch();
});
</script>

<template>
  <o-card simple>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <n-button v-if="currentTable !== 'organization'"
                  v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+ADD']"
                  type="primary"
                  @click="handleAddOrganization"
        >
          {{
            currentTable === 'organization'
                ? $t('system.organization.createOrganization')
                : $t('system.organization.createProject')
          }}
        </n-button>
      </div>
      <div class="flex items-center">
        <n-input :placeholder="$t('system.organization.searchIndexPlaceholder')" class="w-[240px]" clearable/>
        <n-radio-group v-model:value="currentTable" class="ml-[14px]">
          <n-radio-button value="organization">
            {{ $t('system.organization.organizationCount', {count: organizationCount}) }}
          </n-radio-button>
          <n-radio-button value="project">{{
              $t('system.organization.projectCount', {count: projectCount})
            }}
          </n-radio-button>
        </n-radio-group>
      </div>
    </div>
    <div>
      <system-organization v-if="currentTable === 'organization'" ref="orgTableRef"/>
      <system-project v-if="currentTable === 'project'" ref="projectTableRef"/>
    </div>
  </o-card>
  <add-project-modal ref="addProjectModalRef" :visible="projectVisible" @cancel="handleAddProjectCancel"/>
</template>

<style scoped>

</style>