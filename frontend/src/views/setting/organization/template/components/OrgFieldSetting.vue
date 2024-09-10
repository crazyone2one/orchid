<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue'
import FieldSetting from "/@/views/setting/organization/template/components/FieldSetting.vue";
</script>

<template>
  <o-card simple has-breadcrumb>
    <FieldSetting
        mode="organization"
        :delete-permission="['ORGANIZATION_TEMPLATE:READ+DELETE']"
        :update-permission="['ORGANIZATION_TEMPLATE:READ+UPDATE']"
        :create-permission="['ORGANIZATION_TEMPLATE:READ+ADD']"
    />
  </o-card>
</template>

<style scoped>

</style>