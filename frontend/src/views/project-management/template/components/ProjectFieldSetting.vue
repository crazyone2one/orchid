<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue'
import FieldSetting from "/@/views/setting/organization/template/components/FieldSetting.vue";
</script>

<template>
  <o-card simple has-breadcrumb>
    <field-setting mode="project"
                   :delete-permission="['PROJECT_TEMPLATE:READ+DELETE']"
                   :update-permission="['PROJECT_TEMPLATE:READ+UPDATE']"
                   :create-permission="['PROJECT_TEMPLATE:READ+ADD']"/>
  </o-card>

</template>

<style scoped>

</style>