<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue'
import {ref} from "vue";
import SystemProject from '/@/views/setting/system/organizationAndProject/components/SystemProject.vue';
import SystemOrganization from '/@/views/setting/system/organizationAndProject/components/SystemOrganization.vue'

const currentTable = ref('organization');
const orgTableRef = ref<InstanceType<typeof SystemOrganization> | null>(null)
const projectTableRef = ref<InstanceType<typeof SystemProject> | null>(null)
</script>

<template>
  <o-card simple>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <n-button v-if="currentTable !== 'organization'"
                  v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+ADD']"
                  type="primary"
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
            {{ $t('system.organization.organizationCount', {count: 10}) }}
          </n-radio-button>
          <n-radio-button value="project">{{ $t('system.organization.projectCount', {count: 20}) }}</n-radio-button>
        </n-radio-group>
      </div>
    </div>
    <div>
      <system-organization v-if="currentTable === 'organization'" ref="orgTableRef"/>
      <system-project v-if="currentTable === 'project'" ref="projectTableRef"/>
    </div>
  </o-card>
</template>

<style scoped>

</style>